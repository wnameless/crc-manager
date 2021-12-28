/*
 *
 * Copyright 2020 Wei-Ming Wu
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

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.wmw.crc.manager.RestfulPath.Names.CASE_STUDY;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.wnameless.spring.boot.up.web.ModelPolicy;
import com.github.wnameless.spring.boot.up.web.RestfulController;
import com.github.wnameless.spring.boot.up.web.RestfulRoute;
import com.wmw.crc.manager.RestfulPath;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.repository.SubjectRepository;
import com.wmw.crc.manager.service.CaseStudyService;

import net.sf.rubycollect4j.Ruby;

@RequestMapping("/" + CASE_STUDY + "/{id}/unreviewed-visits")
@Controller
public class CaseUnreviewedVisitController
    implements RestfulController<CaseStudy, Long, CaseStudyRepository> {

  @Autowired
  CaseStudyRepository caseStudyRepo;
  @Autowired
  SubjectRepository subjectRepo;

  @Autowired
  CaseStudyService caseService;

  CaseStudy caseStudy;

  @Override
  public void configure(ModelPolicy<CaseStudy> policy) {
    policy.afterInit(item -> caseStudy = firstNonNull(item, new CaseStudy()));
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @GetMapping
  String index(Model model, @PathVariable Long id) {
    List<Subject> subjects = subjectRepo.findAllByCaseStudy(caseStudy);
    subjects = Ruby.Array.of(subjects).keepIf(s -> s.unreviewedVisits() > 0);

    model.addAttribute("subjects", subjects);
    return "cases/unreviewed-visits/index";
  }

  @Override
  public RestfulRoute<Long> getRoute() {
    return RestfulPath.CASE_STUDY;
  }

  @Override
  public CaseStudyRepository getRepository() {
    return caseStudyRepo;
  }

}
