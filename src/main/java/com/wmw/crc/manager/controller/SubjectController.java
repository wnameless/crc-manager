/*
 *
 * Copyright 2018 Wei-Ming Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.wmw.crc.manager.controller;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.wmw.crc.manager.util.EntityUtils.findChildById;
import static com.wmw.crc.manager.util.EntityUtils.findChildByValue;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.wmw.crc.manager.model.Case;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.repository.CaseRepository;
import com.wmw.crc.manager.repository.SubjectRepository;
import com.wmw.crc.manager.service.tsgh.api.Patient;
import com.wmw.crc.manager.service.tsgh.api.TsghApi;
import com.wmw.crc.manager.util.ExcelSubjects;

import net.sf.rubycollect4j.Ruby;
import net.sf.rubycollect4j.RubyArray;

@Controller
public class SubjectController {

  @Autowired
  CaseRepository caseRepo;

  @Autowired
  SubjectRepository subjectRepo;

  @Autowired
  MessageSource messageSource;

  @Autowired
  TsghApi tsghApi;

  @PreAuthorize("@perm.canRead(#caseId)")
  @GetMapping("/cases/{caseId}/subjects/index")
  String index(Model model, @PathVariable("caseId") Long caseId) {
    Case c = caseRepo.getOne(caseId);

    model.addAttribute("case", c);
    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItems", c.getSubjects());
    return "subjects/index";
  }

  @PreAuthorize("@perm.canRead(#caseId)")
  @GetMapping("/cases/{caseId}/subjects")
  String list(Model model, @PathVariable("caseId") Long caseId) {
    Case c = caseRepo.getOne(caseId);

    model.addAttribute("case", c);
    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItems", c.getSubjects());
    return "subjects/list :: list";
  }

  @PreAuthorize("@perm.canWrite(#caseId)")
  @GetMapping("/cases/{caseId}/subjects/new")
  String newItem(Model model, @PathVariable("caseId") Long caseId) {
    Case c = caseRepo.getOne(caseId);

    model.addAttribute("case", c);
    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItem", new Subject());
    return "subjects/new :: new";
  }

  @PreAuthorize("@perm.canWrite(#caseId)")
  @PostMapping("/cases/{caseId}/subjects")
  String create(Model model, @PathVariable("caseId") Long caseId,
      @RequestBody String formData, Locale locale) {
    Case c = caseRepo.getOne(caseId);

    Subject s = new Subject();
    s.setJsonData(formData);
    if (findChildByValue(c.getSubjects(), s.getNationalId(),
        Subject::getNationalId) == null) {
      subjectRepo.save(s);
      c.getSubjects().add(s);
      caseRepo.save(c);
    } else {
      model.addAttribute("message", messageSource.getMessage(
          "ctrl.subject.message.nationalid-existed", new Object[] {}, locale));
    }

    model.addAttribute("case", c);
    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItems", c.getSubjects());
    return "subjects/list :: list";
  }

  @PreAuthorize("@perm.canWrite(#caseId)")
  @PostMapping("/cases/{caseId}/subjects/{id}")
  String update(Model model, @PathVariable("caseId") Long caseId,
      @PathVariable("id") Long id, @RequestBody String formData,
      Locale locale) {
    Case c = caseRepo.getOne(caseId);

    Subject subject = findChildById(c.getSubjects(), id, Subject::getId);
    if (subject != null) {
      String jsonData = subject.getJsonData();
      subject.setJsonData(formData);

      int count = Ruby.Array.of(c.getSubjects()).count(
          s -> Objects.equals(s.getNationalId(), subject.getNationalId()));
      if (count == 1) {
        subjectRepo.save(subject);
      } else {
        subject.setJsonData(jsonData);
        model.addAttribute("message",
            messageSource.getMessage("ctrl.subject.message.nationalid-existed",
                new Object[] {}, locale));
      }
    }

    model.addAttribute("case", c);
    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItems", c.getSubjects());
    return "subjects/list :: list";
  }

  @PreAuthorize("@perm.canRead(#caseId)")
  @GetMapping("/cases/{caseId}/subjects/{id}")
  String show(Model model, @PathVariable("caseId") Long caseId,
      @PathVariable("id") Long id) {
    Case c = caseRepo.getOne(caseId);
    Subject subject = findChildById(c.getSubjects(), id, Subject::getId);

    model.addAttribute("case", c);
    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItem", subject);
    return "subjects/show :: show";
  }

  @PreAuthorize("@perm.canWrite(#caseId)")
  @GetMapping("/cases/{caseId}/subjects/{id}/edit")
  String edit(Model model, @PathVariable("caseId") Long caseId,
      @PathVariable("id") Long id) {
    Case c = caseRepo.getOne(caseId);
    Subject subject = findChildById(c.getSubjects(), id, Subject::getId);

    model.addAttribute("case", c);
    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects/" + id);
    model.addAttribute("jsfItem", subject);
    return "subjects/edit :: edit";
  }

  @PreAuthorize("@perm.canDeleteSubject(#caseId)")
  @GetMapping("/cases/{caseId}/subjects/{id}/delete")
  String delete(Model model, @PathVariable("caseId") Long caseId,
      @PathVariable("id") Long id) {
    Case c = caseRepo.getOne(caseId);
    Subject subject = findChildById(c.getSubjects(), id, Subject::getId);

    if (c.getSubjects().remove(subject)) {
      caseRepo.save(c);
      subjectRepo.delete(subject);
    }

    return "redirect:/cases/" + caseId + "/subjects/index";
  }

  @PreAuthorize("@perm.canWrite(#caseId)")
  @GetMapping("/cases/{caseId}/subjects/{id}/status/{status}")
  String alterStatus(@PathVariable("caseId") Long caseId,
      @PathVariable("id") Long id, @PathVariable("status") String status) {
    Case c = caseRepo.getOne(caseId);
    Subject subject = findChildById(c.getSubjects(), id, Subject::getId);
    subject.setStatus(Subject.Status.fromString(status));
    subjectRepo.save(subject);

    return "redirect:/cases/" + caseId + "/subjects/index";
  }

  @PreAuthorize("@perm.canWrite(#caseId)")
  @PostMapping("/cases/{caseId}/subjects/batchdating")
  String batchDating(RedirectAttributes redirAttrs,
      @PathVariable("caseId") Long caseId,
      @RequestParam("subjectDateType") String subjectDateType,
      @RequestParam(name = "subjectDate", required = false) String subjectDate,
      @RequestParam(name = "subjectIds[]",
          required = false) List<Long> subjectIds,
      Locale locale) {
    Case c = caseRepo.getOne(caseId);
    List<Subject> subjects = c.getSubjects();

    if (isNullOrEmpty(subjectDate)) {
      redirAttrs.addFlashAttribute("message", messageSource.getMessage(
          "ctrl.subject.message.date-unselect", new Object[] {}, locale));
    } else if (subjectIds == null) {
      redirAttrs.addFlashAttribute("message", messageSource.getMessage(
          "ctrl.subject.message.subject-unselect", new Object[] {}, locale));
    }

    if (!isNullOrEmpty(subjectDate) && subjectIds != null) {
      subjects.forEach(s -> {
        if (subjectIds.contains(s.getId())) {
          String jsonData = s.getJsonData();
          JsonObject jo = Json.parse(jsonData).asObject();
          jo.set(subjectDateType, subjectDate);
          s.setJsonData(jo.toString());
          subjectRepo.save(s);
        }
      });
    }

    return "redirect:/cases/" + caseId + "/subjects/index";
  }

  @PreAuthorize("@perm.canWrite(#caseId)")
  @PostMapping(path = "/cases/{caseId}/subjects/index")
  String batchFile(RedirectAttributes redirAttrs,
      @PathVariable("caseId") Long caseId,
      @RequestParam("subjectFile") MultipartFile file) {
    Case c = caseRepo.getOne(caseId);

    ExcelSubjects es = new ExcelSubjects(file);
    if (es.getErrorMessage() == null) {
      List<String> nationalIds =
          Ruby.Array.of(c.getSubjects()).map(Subject::getNationalId).toList();
      Map<Boolean, RubyArray<Subject>> groups = Ruby.Array.of(es.getSubjects())
          .groupBy(s -> nationalIds.contains(s.getNationalId())).toMap();

      if (groups.containsKey(true)) {
        for (Subject s : groups.get(true)) {
          Subject target = findChildByValue(c.getSubjects(), s.getNationalId(),
              Subject::getNationalId);
          target.setJsonData(s.getJsonData());
          subjectRepo.save(target);
        }
      }

      if (groups.containsKey(false)) {
        List<Subject> targets =
            groups.get(false).uniq(s -> s.getNationalId()).toList();
        subjectRepo.saveAll(targets);
        c.getSubjects().addAll(targets);
        caseRepo.save(c);
      }
    } else {
      redirAttrs.addFlashAttribute("message", es.getErrorMessage());
    }

    return "redirect:/cases/" + caseId + "/subjects/index";
  }

  @PreAuthorize("@perm.canWrite(#caseId)")
  @GetMapping("/subjects/query/{nationalId}")
  Patient searchPatient(Model model,
      @PathVariable("nationalId") String nationalId) {
    return tsghApi.serachPatientById(nationalId);
  }

}
