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
package com.wmw.crc.manager;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.CaseStudy.Status;
import com.wmw.crc.manager.model.Contraindication;
import com.wmw.crc.manager.model.Medicine;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.repository.MedicineRepository;
import com.wmw.crc.manager.service.tsgh.api.Drug;
import com.wmw.crc.manager.service.tsgh.api.PatientContraindication;
import com.wmw.crc.manager.service.tsgh.api.SimpleDrug;
import com.wmw.crc.manager.service.tsgh.api.TsghApi;

import lombok.extern.slf4j.Slf4j;
import net.sf.rubycollect4j.Ruby;
import net.sf.rubycollect4j.RubyArray;
import net.sf.rubycollect4j.RubyHash;

@Slf4j
@Component
public class ScheduledTasks {

  @Autowired
  CaseStudyRepository caseRepo;

  @Autowired
  MedicineRepository medicineRepo;

  @Autowired
  TsghApi tsghApi;

  @Scheduled(cron = "0 0 22 * * *")
  void refreshMedicines() {
    List<Drug> drugs;
    try {
      drugs = tsghApi.getDrugs();
    } catch (IOException e) {
      log.error("TsghApi.getDrugs failed.", e);
      return;
    }

    medicineRepo.deleteAll();

    for (Drug drug : drugs) {
      Medicine med = new Medicine();
      med.setName(drug.getName());
      med.setEngName(drug.getEngName());
      med.setHospitalCode(drug.getHospitalCode());
      if (drug.getAtcCode() != null) {
        RubyArray<String> atcCodes = Ruby.Array.of(drug.getAtcCode());
        med.setAtcCode1(atcCodes.at(0));
        med.setAtcCode2(atcCodes.at(1));
        med.setAtcCode3(atcCodes.at(2));
        med.setAtcCode4(atcCodes.at(3));
      }
      medicineRepo.save(med);
    }
  }

  @Scheduled(cron = "0 0 23 * * *")
  void refreshContraindications() {
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
            SimpleDrug sd = new SimpleDrug();
            sd.setPhrase(cd.getPhrase());
            sd.setAtcCode(cd.getAtcCode());
            pc.getDrugs().add(sd);
          }
        }

        try {
          tsghApi.addPatientContraindication(pc);
        } catch (IOException e) {
          log.error("TsghApi.addPatientContraindication for patient("
              + s.getNationalId() + ") failed.", e);
        }
      }
    }
  }

}
