/*
 *
 * Copyright 2018 Wei-Ming Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.wmw.crc.manager.service;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.wnameless.advancedoptional.AdvOpt;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.CaseStudy.Status;
import com.wmw.crc.manager.model.Contraindication;
import com.wmw.crc.manager.model.Medicine;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.repository.MedicineRepository;
import com.wmw.crc.manager.service.tsgh.api.Drug;
import com.wmw.crc.manager.service.tsgh.api.Patient;
import com.wmw.crc.manager.service.tsgh.api.PatientContraindication;
import com.wmw.crc.manager.service.tsgh.api.SimpleDrug;
import com.wmw.crc.manager.service.tsgh.api.TsghApi;
import com.wmw.crc.manager.service.tsgh.api.TsghResponse;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.rubycollect4j.Ruby;
import net.sf.rubycollect4j.RubyArray;
import net.sf.rubycollect4j.RubyHash;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Slf4j
@Service
public class TsghService {

  @Value("${api.tsgh.baseurl}")
  String baseUrl;

  TsghApi tsghApi;

  @Autowired
  CaseStudyRepository caseRepo;

  @Autowired
  MedicineRepository medicineRepo;

  @PostConstruct
  void postConstruct() {
    HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
    interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    OkHttpClient client =
        new OkHttpClient.Builder().addInterceptor(interceptor).build();

    Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl).client(client)
        .addConverterFactory(GsonConverterFactory.create()).build();
    tsghApi = retrofit.create(TsghApi.class);
  }

  public Patient findPatientById(String nationalId) throws IOException {
    Call<TsghResponse<Patient>> call = tsghApi.searchPatient(nationalId);
    Response<TsghResponse<Patient>> res = call.execute();
    TsghResponse<Patient> body = res.body();
    return body == null || body.getData().isEmpty() ? null
        : body.getData().get(0);
  }

  public List<Drug> getDrugs() throws IOException {
    Call<TsghResponse<Drug>> call = tsghApi.listDrugs();
    Response<TsghResponse<Drug>> res = call.execute();
    return res.body().getData();
  }

  public ResponseBody addPatientContraindication(PatientContraindication pc)
      throws IOException {
    Call<ResponseBody> call = tsghApi.addPatientContraindication(pc);
    Response<ResponseBody> res = call.execute();
    return res.body();
  }

  public AdvOpt<Integer> refreshMedicines() {
    List<Drug> drugs;
    try {
      drugs = getDrugs();
      if (!drugs.isEmpty()) medicineRepo.deleteAll();
    } catch (IOException e) {
      log.error("TsghService#getDrugs failed.", e);
      return AdvOpt.ofNullable(null, "TsghService::getDrugs failed.");
    }

    for (Drug drug : drugs) {
      Medicine med = new Medicine();
      med.setName(drug.getName());
      med.setEngName(drug.getEngName());
      med.setScientificName(drug.getScientificName());
      med.setHospitalCode(drug.getHospitalCode());
      if (drug.getAtcCode() != null) {
        RubyArray<String> atcCodes = Ruby.Array.of(drug.getAtcCode());
        med.setAtcCode1(atcCodes.at(0));
        med.setAtcCode2(atcCodes.at(1));
        med.setAtcCode3(atcCodes.at(2));
        med.setAtcCode4(atcCodes.at(3));
      }
      med.setTakekind(drug.getTakekind());
      medicineRepo.save(med);
    }

    return AdvOpt.of(drugs.size(),
        "Total " + drugs.size() + " medicines has been updated.");
  }

  public AdvOpt<ContraindicationRefreshResult> refreshContraindications() {
    ContraindicationRefreshResult crr = new ContraindicationRefreshResult();

    List<CaseStudy> cases = caseRepo.findByStatus(Status.EXEC);
    for (CaseStudy c : cases) {
      RubyHash<Integer, RubyArray<Contraindication>> bundles = Ruby.Array
          .of(c.getContraindications()).groupBy(Contraindication::getBundle);

      for (Subject s : c.getSubjects()) {
        PatientContraindication pc = new PatientContraindication();
        pc.setNationalId(s.getNationalId());
        pc.setIrbName(c.getTrialName());
        pc.setIrbNumber(c.getIrbNumber());
        pc.setPatientId(s.getNationalId());
        pc.setStartDate(c.getExpectedStartDate());
        pc.setEndDate(c.getExpectedEndDate());

        if (bundles.containsKey(s.getContraindicationBundle())) {
          RubyArray<Contraindication> cds =
              bundles.get(s.getContraindicationBundle());
          for (Contraindication cd : cds) {
            List<Medicine> meds = medicineRepo
                .findByNameContainsOrEngNameContainsOrScientificNameContainsAllIgnoreCase(
                    cd.getPhrase(), cd.getPhrase(), cd.getPhrase());

            for (Medicine med : meds) {
              SimpleDrug sd = new SimpleDrug();
              sd.setPhrase(cd.getPhrase());
              sd.setAtcCode(med.getAtcCode1());
              sd.setHospitalCode(med.getHospitalCode());
              sd.setMemo(cd.getMemo());
              pc.getDrugs().add(sd);
            }
          }
        }

        try {
          addPatientContraindication(pc);
          crr.increaseSuccessCount();
        } catch (IOException e) {
          log.error("TsghService#addPatientContraindication for patient("
              + s.getNationalId() + ") failed.", e);
          crr.increaseFailedCount();
        }
      }
    }

    return AdvOpt.of(crr,
        "" + crr.getSuccessCount() + " contraindications has been added. "
            + crr.getFailedCount() + " contraindications are failed.");
  }

  @Data
  public static final class ContraindicationRefreshResult {

    int successCount = 0;

    int failedCount = 0;

    public int increaseSuccessCount() {
      return ++successCount;
    }

    public int increaseFailedCount() {
      return ++failedCount;
    }

  }

}
