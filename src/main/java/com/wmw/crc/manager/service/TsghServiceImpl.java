/*
 *
 * Copyright 2019 Wei-Ming Wu
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.wnameless.advancedoptional.AdvOpt;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.CaseStudy.Status;
import com.wmw.crc.manager.model.Contraindication;
import com.wmw.crc.manager.model.Medicine;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.repository.ContraindicationRepository;
import com.wmw.crc.manager.repository.MedicineRepository;
import com.wmw.crc.manager.service.tsgh.api.Drug;
import com.wmw.crc.manager.service.tsgh.api.PatientContraindication;
import com.wmw.crc.manager.service.tsgh.api.SimpleDrug;
import com.wmw.crc.manager.service.tsgh.api.TsghApi;
import com.wmw.crc.manager.service.tsgh.api.TsghPatient;
import com.wmw.crc.manager.service.tsgh.api.TsghResponse;
import com.wmw.crc.manager.util.JsonNodeUtils;

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

@Profile("!mock")
@Slf4j
@Service
public class TsghServiceImpl implements TsghService {

  @Value("${api.tsgh.baseurl}")
  String baseUrl;
  TsghApi tsghApi;

  @Autowired
  CaseStudyRepository caseRepo;
  @Autowired
  MedicineRepository medicineRepo;
  @Autowired
  ContraindicationRepository contraindicationRepo;

  @Autowired
  SubjectService subjectService;

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

  public Subject queryPatientById(String nationalId) throws IOException {
    Call<TsghResponse<TsghPatient>> call = tsghApi.searchPatient(nationalId);
    Response<TsghResponse<TsghPatient>> res = call.execute();
    TsghResponse<TsghPatient> body = res.body();

    TsghPatient patient = body == null ? null : body.getData();

    Subject subject = new Subject();
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.createObjectNode();

    if (patient.getNationalId() != null) {
      node.put("taiwanId", patient.getNationalId());
      if (patient.getNationalId().length() >= 2
          && patient.getNationalId().charAt(1) == '1') {
        node.put("gender", "男");
        // formData.gender = '男';
      }
      if (patient.getNationalId().length() >= 2
          && patient.getNationalId().charAt(1) == '2') {
        node.put("gender", "女");
        // formData.gender = '女';
      }
    }
    if (patient.getPatientId() != null) {
      node.put("mrn", Ruby.Array.of(patient.getPatientId()).join(","));
      // formData.mrn = patient.patientId.join(',');
    }
    if (patient.getTrialId() != null) {
      node.put("subjectId", Ruby.Array.of(patient.getTrialId()).join(","));
      // formData.subjectId = patient.trialId.join(',');
    }
    if (patient.getName() != null) {
      node.put("lastname", patient.getName());
      // formData.lastname = patient.name;
    }
    if (patient.getBirthday() != null) {
      if (patient.getBirthday().length() == 7) {
        String year = patient.getBirthday().substring(0, 3);
        String month = patient.getBirthday().substring(3, 2);
        String day = patient.getBirthday().substring(5, 2);

        node.put("birthDate",
            "" + (Integer.parseInt(year) + 1911) + "-" + month + "-" + day);
        // formData.birthDate = `${year}-${month}-${day}`;
      } else if (patient.getBirthday().length() == 8) {
        String year = patient.getBirthday().substring(0, 4);
        String month = patient.getBirthday().substring(4, 2);
        String day = patient.getBirthday().substring(6, 2);

        node.put("birthDate", "" + year + "-" + month + "-" + day);
        // formData.birthDate = `${year}-${month}-${day}`;
      }
    }
    if (patient.getPhone() != null && !patient.getPhone().isEmpty()) {
      node.put("telephone1", patient.getPhone());
      // formData.telephone1 = patient.phone;
    }
    if (patient.getAddress() != null) {
      node.put("address", patient.getAddress());
      // formData.address = patient.address;
    }

    subject.setFormData(node);
    return subject;
  }

  public List<Drug> getDrugs() throws IOException {
    Call<TsghResponse<List<Drug>>> call = tsghApi.listDrugs();
    Response<TsghResponse<List<Drug>>> res = call.execute();
    return res.body().getData();
  }

  public Response<ResponseBody> addPatientContraindication(
      PatientContraindication pc) throws IOException {
    Call<ResponseBody> call = tsghApi.addPatientContraindication(pc);
    Response<ResponseBody> res = call.execute();
    return res;
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

    List<CaseStudy> cases = caseRepo.findAllByStatus(Status.EXEC);
    for (CaseStudy c : cases) {
      List<Contraindication> contraindications =
          contraindicationRepo.findAllByCaseStudy(c);

      RubyHash<Integer, RubyArray<Contraindication>> bundles =
          Ruby.Array.of(contraindications).groupBy(Contraindication::getBundle);

      List<Subject> subjects = subjectService.findOngoingSubjects(c);
      for (Subject s : subjects) {
        PatientContraindication pc = new PatientContraindication();
        pc.setNationalId(s.getNationalId());
        pc.setIrbName(c.getTrialName());
        pc.setIrbNumber(c.getIrbNumber());
        pc.setPatientId(s.getPatientId());
        pc.setStartDate(LocalDate.parse(c.getExpectedStartDate())
            .format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        pc.setEndDate(LocalDate.parse(c.getExpectedEndDate())
            .format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        pc.setPiName(
            JsonNodeUtils.findFirstAsString(c.getFormData().get("PI"), "name"));
        pc.setPiPhone(JsonNodeUtils.findFirstAsString(c.getFormData().get("PI"),
            "phone"));

        if (bundles.containsKey(s.getContraindicationBundle())) {
          RubyArray<Contraindication> cds =
              bundles.get(s.getContraindicationBundle());
          for (Contraindication cd : cds) {
            List<Medicine> meds = medicineRepo
                .findByNameContainsOrEngNameContainsOrScientificNameContainsAllIgnoreCase(
                    cd.getPhrase(), cd.getPhrase(), cd.getPhrase());

            for (Medicine med : meds) {
              if (cd.getTakekinds().contains(med.getTakekind())) {
                SimpleDrug sd = new SimpleDrug();
                sd.setPhrase(cd.getPhrase());
                sd.setAtcCode(med.getAtcCode1());
                sd.setHospitalCode(med.getHospitalCode());
                sd.setMemo(cd.getMemo());
                pc.getDrugs().add(sd);
              }
            }
          }
        }

        try {
          Response<ResponseBody> res = addPatientContraindication(pc);

          if (res.isSuccessful()) {
            crr.increaseSuccessCount();
          } else {
            log.error("TsghService#addPatientContraindication for patient("
                + s.getNationalId() + ") failed.");
            crr.increaseFailedCount();
          }
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

}
