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

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.wmw.crc.manager.model.RestfulModel.Names.CASE_STUDY;
import static com.wmw.crc.manager.model.RestfulModel.Names.SUBJECT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.wnameless.spring.common.ModelOption;
import com.github.wnameless.spring.common.NestedRestfulController;
import com.github.wnameless.spring.common.RestfulRoute;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.model.Visit;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.repository.SubjectRepository;
import com.wmw.crc.manager.repository.VisitRepository;

import net.sf.rubycollect4j.Ruby;

@RequestMapping("/" + CASE_STUDY + "/{parentId}/" + SUBJECT)
@Controller
public class VisitController implements NestedRestfulController< //
    CaseStudy, Long, CaseStudyRepository, //
    Subject, Long, SubjectRepository> {

  @Autowired
  CaseStudyRepository caseRepo;
  @Autowired
  SubjectRepository subjectRepo;
  @Autowired
  VisitRepository visitRepo;

  CaseStudy caseStudy;
  Subject subject;

  @Override
  public void configure(ModelOption<CaseStudy> parentInitOption,
      ModelOption<Subject> childInitOption,
      ModelOption<? extends Iterable<Subject>> childrenInitOption) {
    parentInitOption.afterInitAction(p -> caseStudy = p);
    childInitOption.afterInitAction(c -> subject = firstNonNull(c, new Subject()));
    childrenInitOption.disable();
  }

  @PreAuthorize("@perm.canRead(#parentId)")
  @GetMapping("/{id}/visits")
  String index(Model model, @PathVariable Long parentId,
      @PathVariable Long id) {
    List<Subject> subjects = subjectRepo.findAllByCaseStudy(caseStudy);
    Subject subject = Ruby.Array.of(subjects).find(s -> s.getId().equals(id));

    model.addAttribute("visits", subject.getVisits());
    return "visits/index";
  }

  @PreAuthorize("@perm.canWrite(#parentId)")
  @PutMapping(path = "/{id}/visits", produces = APPLICATION_JSON_VALUE)
  @ResponseBody
  Boolean checkReviewed(@PathVariable Long parentId, @PathVariable Long id,
      @RequestParam Long visitId) {
    Visit visit =
        Ruby.Array.of(subject.getVisits()).find(v -> v.getId().equals(visitId));
    visit.setReviewed(!visit.isReviewed());
    visitRepo.save(visit);

    return visit.isReviewed();
  }

  @Override
  public Function<CaseStudy, RestfulRoute<Long>> getRoute() {
    return (caseStudy) -> RestfulRoute.of(caseStudy.joinPath(SUBJECT));
  }

  @Override
  public CaseStudyRepository getParentRepository() {
    return caseRepo;
  }

  @Override
  public SubjectRepository getChildRepository() {
    return subjectRepo;
  }

  @Override
  public BiPredicate<CaseStudy, Subject> getPaternityTesting() {
    return (p, c) -> getChildRepository().existsByIdAndCaseStudy(c.getId(), p);

  }

  @Override
  public Iterable<Subject> getChildren(CaseStudy parent) {
    return getChildRepository().findAllByCaseStudy(parent);
  }

}
