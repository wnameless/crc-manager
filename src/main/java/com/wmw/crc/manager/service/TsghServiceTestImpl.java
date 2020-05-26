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
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.wnameless.advancedoptional.AdvOpt;
import com.google.common.base.Strings;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.CaseStudy.Status;
import com.wmw.crc.manager.model.Contraindication;
import com.wmw.crc.manager.model.Medicine;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.repository.ContraindicationRepository;
import com.wmw.crc.manager.repository.MedicineRepository;
import com.wmw.crc.manager.repository.SubjectRepository;
import com.wmw.crc.manager.service.tsgh.api.Drug;
import com.wmw.crc.manager.service.tsgh.api.PatientContraindication;
import com.wmw.crc.manager.service.tsgh.api.SimpleDrug;

import lombok.extern.slf4j.Slf4j;
import net.sf.rubycollect4j.Ruby;
import net.sf.rubycollect4j.RubyArray;
import net.sf.rubycollect4j.RubyHash;
import okhttp3.ResponseBody;
import retrofit2.Response;

@Profile("mock")
@Slf4j
@Service
public class TsghServiceTestImpl implements TsghService {

  @Autowired
  CaseStudyRepository caseStudyRepo;
  @Autowired
  MedicineRepository medicineRepo;
  @Autowired
  ContraindicationRepository contraindicationRepo;
  @Autowired
  SubjectRepository subjectRepo;

  @Override
  public Subject queryPatientById(String nationalId) throws IOException {
    Subject subject = new Subject();

    if (Strings.isNullOrEmpty(nationalId)) return subject;

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.createObjectNode();

    node.put("taiwanId", nationalId);
    node.put("birthDate", "1980-01-01");
    if (nationalId.length() >= 2 && nationalId.charAt(1) == '1') {
      node.put("gender", "男");
      // formData.gender = '男';
    }
    if (nationalId.length() >= 2 && nationalId.charAt(1) == '2') {
      node.put("gender", "女");
      // formData.gender = '女';
    }

    subject.setFormData(node);
    // subject.setNationalId(nationalId);
    // subject.("19800101");
    return subject;
  }

  @Override
  public List<Drug> getDrugs() throws IOException {
    return new ArrayList<>();
  }

  @Override
  public Response<ResponseBody> addPatientContraindication(
      PatientContraindication pc) throws IOException {
    System.out.println(pc);
    return null;
  }

  @Override
  public AdvOpt<Integer> refreshMedicines() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public AdvOpt<ContraindicationRefreshResult> refreshContraindications() {
    ContraindicationRefreshResult crr = new ContraindicationRefreshResult();

    List<CaseStudy> cases = caseStudyRepo.findAllByStatus(Status.EXEC);
    for (CaseStudy c : cases) {
      List<Contraindication> contraindications =
          contraindicationRepo.findAllByCaseStudy(c);

      RubyHash<Integer, RubyArray<Contraindication>> bundles =
          Ruby.Array.of(contraindications).groupBy(Contraindication::getBundle);

      List<Subject> subjects = subjectRepo.findAllByCaseStudy(c);
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

          if (res != null && res.isSuccessful()) {
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
