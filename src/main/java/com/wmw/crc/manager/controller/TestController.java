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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.wnameless.spring.react.SimpleReactJsonSchemaForm;
import com.google.common.base.Objects;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.repository.SubjectRepository;
import com.wmw.crc.manager.service.NewVisit;
import com.wmw.crc.manager.service.VisitService;
import com.wmw.crc.manager.service.tsgh.TsghService;

import net.sf.rubycollect4j.Ruby;

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
  @GetMapping(path = "test/visits/recount")
  @ResponseBody
  String reCountCaseStudyURVs() {
    visitService.reCountCaseStudyURVs();

    return "CaseStudy URVs recounted";
  }

  @PreAuthorize("@perm.isAdmin()")
  @GetMapping("/json/cases/{id}")
  @ResponseBody
  SimpleReactJsonSchemaForm caseJsonScheme(@PathVariable Long id) {
    CaseStudy cs = caseStudyRepo.findById(id).get();
    SimpleReactJsonSchemaForm rsjf = new SimpleReactJsonSchemaForm();

    rsjf.setFormData(cs.getFormData());
    rsjf.setSchema(cs.getSchema());
    rsjf.setUiSchema(cs.getUiSchema());

    return rsjf;
  }

  @PreAuthorize("@perm.isAdmin()")
  @GetMapping("test/subjects/checkcomplete")
  @ResponseBody
  Integer checkComplete() {
    int count = 0;

    List<Subject> subjects = Ruby.Array.copyOf(subjectRepo.findAll()).toList();
    for (Subject s : subjects) {
      String cDate = s.getCompleteDate();

      s.setFormData(s.getFormData());
      subjectRepo.save(s);

      if (!Objects.equal(cDate, s.getCompleteDate())) {
        count++;
      }
    }

    return count;
  }

  @PreAuthorize("@perm.isAdmin()")
  @GetMapping("test/formdata/clean")
  @ResponseBody
  Integer cleanFormData() {
    int count = 0;

    for (CaseStudy cs : caseStudyRepo.findAll()) {
      ObjectNode formData = (ObjectNode) cs.getFormData();

      if (formData.get("dohFlg").isBoolean()) {
        formData.remove("dohFlg");

        cs.setFormData(formData);
        caseStudyRepo.save(cs);
        count++;
      }
    }

    return count;
  }

}
