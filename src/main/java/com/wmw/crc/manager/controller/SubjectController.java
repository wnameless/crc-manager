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
package com.wmw.crc.manager.controller;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.wmw.crc.manager.RestfulPath.Names.CASE_STUDY;
import static com.wmw.crc.manager.RestfulPath.Names.SUBJECT;
import static net.sf.rubycollect4j.RubyObject.isBlank;
import static net.sf.rubycollect4j.RubyObject.isPresent;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.wnameless.advancedoptional.AdvOpt;
import com.github.wnameless.spring.common.web.ModelPolicy;
import com.github.wnameless.spring.common.web.NestedRestfulController;
import com.github.wnameless.spring.common.web.RestfulRoute;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.repository.SubjectRepository;
import com.wmw.crc.manager.service.ExcelSubjectUploadService;
import com.wmw.crc.manager.service.ExcelSubjects;
import com.wmw.crc.manager.service.I18nService;
import com.wmw.crc.manager.service.SubjectService;
import com.wmw.crc.manager.service.tsgh.TsghService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/" + CASE_STUDY + "/{parentId}/" + SUBJECT)
@Controller
public class SubjectController implements NestedRestfulController< //
    CaseStudy, Long, CaseStudyRepository, //
    Subject, Long, SubjectRepository> {

  @Autowired
  CaseStudyRepository caseStudyRepo;
  @Autowired
  SubjectRepository subjectRepo;
  @Autowired
  SubjectService subjectService;
  @Autowired
  ExcelSubjectUploadService uploadService;
  @Autowired
  TsghService tsghService;
  @Autowired
  I18nService i18n;

  CaseStudy caseStudy;
  Subject subject;

  @Override
  public void configure(ModelPolicy<CaseStudy> parentPolicy,
      ModelPolicy<Subject> childPolicy,
      ModelPolicy<Iterable<Subject>> childrenPolicy) {
    parentPolicy.afterInit(p -> caseStudy = p);
    childPolicy.afterInit(c -> subject = firstNonNull(c, new Subject()));
  }

  @PreAuthorize("@perm.canRead(#parentId)")
  @GetMapping
  String index(@PathVariable Long parentId) {
    return "subjects/index";
  }

  @PreAuthorize("@perm.canRead(#parentId)")
  @GetMapping(produces = APPLICATION_JSON_VALUE)
  String indexJS(@PathVariable Long parentId) {
    return "subjects/list :: partial";
  }

  @PreAuthorize("@perm.canWrite(#parentId)")
  @GetMapping("/new")
  String newJS(@PathVariable Long parentId) {
    return "subjects/new :: partial";
  }

  @PreAuthorize("@perm.canWrite(#parentId)")
  @PostMapping("/batch")
  String batchCreate(@PathVariable Long parentId,
      @RequestParam MultipartFile subjectFile, RedirectAttributes redirAttrs) {
    ExcelSubjects es = uploadService.fromMultipartFile(subjectFile);
    if (es.getErrorMessage() == null) {
      subjectService.batchCreate(caseStudy, es);
    } else {
      redirAttrs.addFlashAttribute("message", es.getErrorMessage());
    }

    return "redirect:" + getRoute().apply(caseStudy).getIndexPath();
  }

  @PreAuthorize("@perm.canWrite(#parentId)")
  @PostMapping
  String createJS(Model model, @PathVariable Long parentId,
      @RequestBody JsonNode formData) {
    subject = new Subject(formData);
    AdvOpt<Subject> sOpt = subjectService.createSubject(caseStudy, subject);

    if (sOpt.isAbsent()) {
      if (sOpt.hasMessage()) {
        model.addAttribute("message", i18n.msg(sOpt.getMessage()));
      }
    } else {
      updateChildrenByParent(model, caseStudy);
    }
    return "subjects/list :: partial";
  }

  @PreAuthorize("@perm.canWrite(#parentId)")
  @PostMapping("/{id}")
  String updateJS(Model model, @PathVariable Long parentId,
      @RequestBody JsonNode formData) {
    AdvOpt<Subject> sOpt = subjectService.updateSubject(subject, formData);

    if (sOpt.isAbsent()) {
      if (sOpt.hasMessage()) {
        model.addAttribute("message", i18n.msg(sOpt.getMessage()));
      }
    } else {
      updateChildrenByParent(model, caseStudy);
    }
    return "subjects/list :: partial";
  }

  @PreAuthorize("@perm.canRead(#parentId)")
  @GetMapping("/{id}")
  String showJS(@PathVariable Long parentId) {
    return "subjects/show :: partial";
  }

  @PreAuthorize("@perm.canWrite(#parentId)")
  @GetMapping("/{id}/edit")
  String editJS(@PathVariable Long parentId) {
    return "subjects/edit :: partial";
  }

  @PreAuthorize("@perm.canDeleteSubject(#parentId)")
  @DeleteMapping("/{id}")
  String delete(@PathVariable Long parentId) {
    if (subject.getId() != null) {
      subjectRepo.delete(subject);
    }

    return "redirect:" + getRoute().apply(caseStudy).getIndexPath();
  }

  @PreAuthorize("@perm.canDeleteSubject(#parentId)")
  @DeleteMapping(path = "/{id}", produces = APPLICATION_JSON_VALUE)
  String deleteJS(Model model, @PathVariable Long parentId) {
    if (subject.getId() != null) {
      subjectRepo.delete(subject);

      updateChildrenByParent(model, caseStudy);
    }

    return "subjects/list :: partial";
  }

  @PreAuthorize("@perm.canWrite(#parentId)")
  @PutMapping("/{id}")
  @ResponseBody
  Integer alterBundle(@PathVariable Long parentId,
      @RequestParam Integer bundle) {
    if (subject.getId() != null) {
      subject.setContraindicationBundle(bundle);
      subjectRepo.save(subject);
    }

    return subject.getContraindicationBundle();
  }

  @PreAuthorize("@perm.canWrite(#parentId)")
  @PostMapping("/batchpatch")
  String batchPatch(@PathVariable Long parentId,
      @RequestParam String subjectDateType,
      @RequestParam(required = false) String subjectDate,
      @RequestParam(name = "oofValues", required = false) List<Long> subjectIds,
      @RequestParam Integer bundleNumber, Locale locale,
      RedirectAttributes redirAttrs) {
    if (!subjectDateType.equals("bundleNumber") && isBlank(subjectDate)) {
      redirAttrs.addFlashAttribute("message", i18n.subjectDateUnselect(locale));
    } else if (subjectIds == null) {
      redirAttrs.addFlashAttribute("message", i18n.subjectUnselect(locale));
    }

    Iterable<Subject> subjects = getChildren(caseStudy);
    if (isPresent(subjectDate) && subjectIds != null
        && !subjectDateType.equals("bundleNumber")) {
      subjects.forEach(s -> {
        if (subjectIds.contains(s.getId())) {
          ObjectNode jsonData = (ObjectNode) s.getFormData();
          jsonData.put(subjectDateType, subjectDate);
          s.setFormData(jsonData);
          subjectRepo.save(s);
        }
      });
    }

    if (subjectDateType.equals("bundleNumber") && subjectIds != null) {
      subjects.forEach(s -> {
        if (subjectIds.contains(s.getId())) {
          s.setContraindicationBundle(bundleNumber);
          subjectRepo.save(s);
        }
      });
    }

    return "redirect:" + getRoute().apply(caseStudy).getIndexPath();
  }

  @PreAuthorize("@perm.canWrite(#parentId)")
  @GetMapping("/query/{nationalId}")
  String searchPatient(Model model, @PathVariable Long parentId,
      @PathVariable String nationalId) {
    Subject subject;
    try {
      subject = tsghService.queryPatientById(nationalId);
    } catch (IOException e) {
      log.error("Patient seaching failed!", e);
      subject = new Subject();
    }

    updateChild(model, subject);
    if (subject.getNationalId() == null) {
      model.addAttribute("message", i18n.msg("ui.subject.message.id-no-data"));
    }
    return "subjects/new :: partial";
  }

  @PreAuthorize("@perm.canWrite(#parentId)")
  @GetMapping("/uploadexample")
  @ResponseBody
  HttpEntity<byte[]> downloadExample(@PathVariable Long parentId)
      throws IOException {
    return subjectService.createDownloadableUploadExample();
  }

  @Override
  public Function<CaseStudy, RestfulRoute<Long>> getRoute() {
    return (caseStudy) -> RestfulRoute.of(caseStudy.joinPath(SUBJECT));
  }

  @Override
  public CaseStudyRepository getParentRepository() {
    return caseStudyRepo;
  }

  @Override
  public SubjectRepository getChildRepository() {
    return subjectRepo;
  }

  @Override
  public BiPredicate<CaseStudy, Subject> getPaternityTesting() {
    return (p, c) -> Objects.equals(p, c.getCaseStudy());

  }

  @Override
  public Iterable<Subject> getChildren(CaseStudy parent) {
    return getChildRepository().findAllByCaseStudy(parent);
  }

}
