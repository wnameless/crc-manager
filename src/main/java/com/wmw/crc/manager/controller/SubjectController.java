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

import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.Resources;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.repository.SubjectRepository;
import com.wmw.crc.manager.service.ExcelSubjectUploadService;
import com.wmw.crc.manager.service.I18nService;
import com.wmw.crc.manager.service.SubjectService;
import com.wmw.crc.manager.service.TsghService;
import com.wmw.crc.manager.service.tsgh.api.Patient;
import com.wmw.crc.manager.util.ExcelSubjects;

import lombok.extern.slf4j.Slf4j;
import net.sf.rubycollect4j.Ruby;

@Slf4j
@Controller
public class SubjectController {

  @Autowired
  CaseStudyRepository caseRepo;

  @Autowired
  SubjectRepository subjectRepo;

  @Autowired
  ExcelSubjectUploadService uploadService;

  @Autowired
  TsghService tsghService;

  @Autowired
  SubjectService subjectService;

  @Autowired
  I18nService i18n;

  @PreAuthorize("@perm.canRead(#caseId)")
  @GetMapping("/cases/{caseId}/subjects/index")
  String index(Model model, @PathVariable("caseId") Long caseId) {
    CaseStudy c = caseRepo.findById(caseId).get();
    List<Subject> subjects = subjectRepo.findAllByCaseStudy(c);

    model.addAttribute("case", c);
    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItems", subjects);
    return "subjects/index";
  }

  @PreAuthorize("@perm.canRead(#caseId)")
  @GetMapping("/cases/{caseId}/subjects")
  String list(Model model, @PathVariable("caseId") Long caseId) {
    CaseStudy c = caseRepo.findById(caseId).get();
    List<Subject> subjects = subjectRepo.findAllByCaseStudy(c);

    model.addAttribute("case", c);
    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItems", subjects);
    return "subjects/list :: list";
  }

  @PreAuthorize("@perm.canWrite(#caseId)")
  @GetMapping("/cases/{caseId}/subjects/new")
  String newItem(Model model, @PathVariable("caseId") Long caseId) {
    CaseStudy c = caseRepo.findById(caseId).get();

    model.addAttribute("case", c);
    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItem", new Subject());
    return "subjects/new :: new";
  }

  @PreAuthorize("@perm.canWrite(#caseId)")
  @PostMapping("/cases/{caseId}/subjects")
  String create(Model model, @PathVariable("caseId") Long caseId,
      @RequestBody JsonNode formData, Locale locale) {
    CaseStudy c = caseRepo.findById(caseId).get();

    Subject s = new Subject();
    s.setFormData(formData);
    s = subjectService.createSubject(c, s);

    if (s == null) {
      model.addAttribute("message", i18n.subjectNationalIDExisted(locale));
    }
    model.addAttribute("case", c);
    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItems", subjectRepo.findAllByCaseStudy(c));
    return "subjects/list :: list";
  }

  @PreAuthorize("@perm.canWrite(#caseId)")
  @PostMapping(path = "/cases/{caseId}/subjects/index")
  String batchCreate(RedirectAttributes redirAttrs,
      @PathVariable("caseId") Long caseId,
      @RequestParam("subjectFile") MultipartFile file) {
    CaseStudy c = caseRepo.findById(caseId).get();

    ExcelSubjects es = uploadService.fromMultipartFile(file);
    if (es.getErrorMessage() == null) {
      subjectService.batchCreate(c, es);
    } else {
      redirAttrs.addFlashAttribute("message", es.getErrorMessage());
    }

    return "redirect:/cases/" + caseId + "/subjects/index";
  }

  @PreAuthorize("@perm.canWrite(#caseId)")
  @PostMapping("/cases/{caseId}/subjects/{id}")
  String update(Model model, @PathVariable("caseId") Long caseId,
      @PathVariable("id") Long id, @RequestBody JsonNode formData,
      Locale locale) {
    CaseStudy c = caseRepo.findById(caseId).get();

    Subject subject = subjectRepo.findByIdAndCaseStudy(id, c);

    boolean dropoutSafe = subjectService.secureDropoutDate(subject, formData);
    if (!dropoutSafe) {
      model.addAttribute("message", i18n.subjectDropoutDateCannotClear(locale));
    }

    if (subject != null && dropoutSafe) {
      subject = subjectService.updateSubject(c, subject, formData);

      if (subject == null) {
        model.addAttribute("message", i18n.subjectNationalIDExisted(locale));
      }
    }

    model.addAttribute("case", c);
    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItems", subjectRepo.findAllByCaseStudy(c));
    return "subjects/list :: list";
  }

  @PreAuthorize("@perm.canRead(#caseId)")
  @GetMapping("/cases/{caseId}/subjects/{id}")
  String show(Model model, @PathVariable("caseId") Long caseId,
      @PathVariable("id") Long id) {
    CaseStudy c = caseRepo.findById(caseId).get();
    Subject subject = subjectRepo.findByIdAndCaseStudy(id, c);

    model.addAttribute("case", c);
    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItem", subject);
    return "subjects/show :: show";
  }

  @PreAuthorize("@perm.canWrite(#caseId)")
  @GetMapping("/cases/{caseId}/subjects/{id}/edit")
  String edit(Model model, @PathVariable("caseId") Long caseId,
      @PathVariable("id") Long id) {
    CaseStudy c = caseRepo.findById(caseId).get();
    Subject subject = subjectRepo.findByIdAndCaseStudy(id, c);

    model.addAttribute("case", c);
    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects/" + id);
    model.addAttribute("jsfItem", subject);
    return "subjects/edit :: edit";
  }

  @PreAuthorize("@perm.canDeleteSubject(#caseId)")
  @GetMapping("/cases/{caseId}/subjects/{id}/delete")
  String delete(Model model, @PathVariable("caseId") Long caseId,
      @PathVariable("id") Long id) {
    CaseStudy c = caseRepo.findById(caseId).get();
    Subject subject = subjectRepo.findByIdAndCaseStudy(id, c);

    if (subject != null) {
      subjectRepo.delete(subject);
    }

    return "redirect:/cases/" + caseId + "/subjects/index";
  }

  @PreAuthorize("@perm.canWrite(#caseId)")
  @GetMapping("/cases/{caseId}/subjects/{id}/status/{status}")
  String alterStatus(@PathVariable("caseId") Long caseId,
      @PathVariable("id") Long id, @PathVariable("status") String status) {
    CaseStudy c = caseRepo.findById(caseId).get();
    Subject subject = subjectRepo.findByIdAndCaseStudy(id, c);

    subject.setStatus(Subject.Status.fromString(status));
    subjectRepo.save(subject);

    return "redirect:/cases/" + caseId + "/subjects/index";
  }

  @PreAuthorize("@perm.canWrite(#caseId)")
  @GetMapping("cases/{caseId}/subjects/{id}/bundle/{bundleNumber}")
  String alterBundle(Model model, @PathVariable("caseId") Long caseId,
      @PathVariable("id") Long id,
      @PathVariable("bundleNumber") Integer bundleNumber) {
    CaseStudy c = caseRepo.findById(caseId).get();
    List<Subject> subjects = subjectRepo.findAllByCaseStudy(c);

    Subject subject = Ruby.Array.of(subjects).find(s -> s.getId().equals(id));
    if (subject != null) {
      subject.setContraindicationBundle(bundleNumber);
      subjectRepo.save(subject);
    }

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
      @RequestParam(name = "bundleNumber") Integer bundleNumber,
      Locale locale) {
    CaseStudy c = caseRepo.findById(caseId).get();
    List<Subject> subjects = subjectRepo.findAllByCaseStudy(c);

    if (!subjectDateType.equals("bundleNumber") && isNullOrEmpty(subjectDate)) {
      redirAttrs.addFlashAttribute("message", i18n.subjectDateUnselect(locale));
    } else if (subjectIds == null) {
      redirAttrs.addFlashAttribute("message", i18n.subjectUnselect(locale));
    }

    if (!isNullOrEmpty(subjectDate) && subjectIds != null
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

    return "redirect:/cases/" + caseId + "/subjects/index";
  }

  @PreAuthorize("@perm.canWrite(#caseId)")
  @GetMapping("/cases/{caseId}/subjects/query/{nationalId}")
  @ResponseBody
  Patient searchPatient(@PathVariable("caseId") Long caseId,
      @PathVariable("nationalId") String nationalId) {
    Patient patient;
    try {
      patient = tsghService.findPatientById(nationalId);
      patient.setNationalId(nationalId);
    } catch (IOException e) {
      log.error("Patient seaching failed!", e);
      patient = new Patient();
    }

    return patient;
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @GetMapping("/cases/{id}/subjects/uploadexample")
  @ResponseBody
  HttpEntity<byte[]> downloadExample(Model model, @PathVariable("id") Long id)
      throws IOException {
    URL exampleUrl = Resources.getResource("examples/三總受試者名單範本.xlsx");

    byte[] dataByteArray = Resources.toByteArray(exampleUrl);

    HttpHeaders header = new HttpHeaders();
    header.setContentType(MediaType.valueOf(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    header.set(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=" + URLEncoder.encode("三總受試者名單範本.xlsx", "UTF-8"));
    header.setContentLength(dataByteArray.length);

    return new HttpEntity<byte[]>(dataByteArray, header);
  }

}
