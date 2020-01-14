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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.model.Visit;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.repository.SubjectRepository;
import com.wmw.crc.manager.repository.VisitRepository;

import net.sf.rubycollect4j.Ruby;

@Controller
public class VisitController {

  @Autowired
  CaseStudyRepository caseRepo;

  @Autowired
  SubjectRepository subjectRepo;

  @Autowired
  VisitRepository visitRepo;

  @Autowired
  MessageSource messageSource;

  @PreAuthorize("@perm.canRead(#caseId)")
  @GetMapping("/cases/{caseId}/subjects/{id}/visits")
  String index(Model model, @PathVariable("caseId") Long caseId,
      @PathVariable("id") Long id) {
    CaseStudy c = caseRepo.findById(caseId).get();
    List<Subject> subjects = subjectRepo.findAllByCaseStudy(c);
    Subject subject = Ruby.Array.of(subjects).find(s -> s.getId().equals(id));

    model.addAttribute("case", c);
    model.addAttribute("subject", subject);
    model.addAttribute("visits", subject.getVisits());
    return "visits/index";
  }

  @PreAuthorize("@perm.canWrite(#caseId)")
  @GetMapping("/cases/{caseId}/subjects/{subjectId}/visits/{id}")
  @ResponseBody
  Boolean checkReviewed(Model model, @PathVariable("caseId") Long caseId,
      @PathVariable("subjectId") Long subjectId, @PathVariable("id") Long id) {
    CaseStudy c = caseRepo.findById(caseId).get();
    List<Subject> subjects = subjectRepo.findAllByCaseStudy(c);
    Subject subject =
        Ruby.Array.of(subjects).find(s -> s.getId().equals(subjectId));

    Visit visit =
        Ruby.Array.of(subject.getVisits()).find(v -> v.getId().equals(id));
    visit.setReviewed(!visit.isReviewed());
    visitRepo.save(visit);

    return visit.isReviewed();
  }

}
