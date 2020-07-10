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
package com.wmw.crc.manager.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.repository.SubjectRepository;
import com.wmw.crc.manager.service.NewVisit;
import com.wmw.crc.manager.service.VisitService;
import com.wmw.crc.manager.service.tsgh.TsghService;

@Profile("test")
@Controller
public class TestController {

  @Autowired
  CaseStudyRepository caseStudyRepo;
  @Autowired
  SubjectRepository subjectRepo;
  @Autowired
  TsghService tsghService;
  @Autowired
  VisitService visitService;

  @PreAuthorize("@perm.isAdmin()")
  @GetMapping("test/medicines/refresh")
  @ResponseBody
  String refreshMedicines() {
    return tsghService.refreshMedicines().getMessage();
  }

  @PreAuthorize("@perm.isAdmin()")
  @GetMapping("test/contraindications/refresh")
  @ResponseBody
  String refreshContraindications() {
    return tsghService.refreshContraindications().getMessage();
  }

  @PreAuthorize("@perm.isAdmin()")
  @GetMapping("test/visits/send")
  @ResponseBody
  List<String> sendVisitEmails() {
    return visitService.sendVisitEmails();
  }

  @PreAuthorize("@perm.isAdmin()")
  @GetMapping(path = "test/visits/add")
  @ResponseBody
  String addFakeVisit() {
    NewVisit newVisit = new NewVisit();

    newVisit.setIrbNumber("123456789");
    newVisit.setRoom("F");
    newVisit.setNationalId("A111222333");
    newVisit.setDoctor("Faker");
    newVisit.setDivision("Fake");
    newVisit.setContraindicationSuspected(true);
    newVisit.setDate(LocalDate.now().plusDays(1));

    visitService.addVisit(newVisit);

    return "Visit added";
  }

  @PreAuthorize("@perm.isAdmin()")
  @GetMapping("/cases/repopulate")
  @ResponseBody
  String caseRepopulate() {
    List<CaseStudy> css = caseStudyRepo.findAll();

    for (CaseStudy cs : css) {
      cs.setExpectedStartDate(null);
      cs.setExpectedEndDate(null);
      cs.setFormData(cs.getFormData());
      caseStudyRepo.save(cs);
    }

    return "Refreshed";
  }

}
