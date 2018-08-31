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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.wmw.crc.manager.model.Case;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.repository.CaseRepository;
import com.wmw.crc.manager.repository.SubjectRepository;
import com.wmw.crc.manager.service.tsgh.api.Patient;
import com.wmw.crc.manager.service.tsgh.api.TsghApi;
import com.wmw.crc.manager.util.ExcelSubjects;

@Controller
public class SubjectController {

  @Autowired
  CaseRepository caseRepo;

  @Autowired
  SubjectRepository subjectRepo;

  @Autowired
  TsghApi tsghApi;

  @GetMapping(path = "/cases/{caseId}/subjects/index")
  String index(Model model, @PathVariable("caseId") Long caseId) {
    Case c = caseRepo.findOne(caseId);
    List<Subject> subjects = c.getSubjects();

    model.addAttribute("case", c);
    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItems", subjects);
    return "subjects/index";
  }

  @GetMapping(path = "/cases/{caseId}/subjects")
  String list(Model model, @PathVariable("caseId") Long caseId) {
    Case c = caseRepo.findOne(caseId);
    List<Subject> subjects = c.getSubjects();

    model.addAttribute("case", c);
    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItems", subjects);
    return "subjects/list :: list";
  }

  @GetMapping(path = "/cases/{caseId}/subjects/new")
  String newItem(Model model, @PathVariable("caseId") Long caseId) {
    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItem", new Subject());
    return "subjects/new :: new";
  }

  @PostMapping(path = "/cases/{caseId}/subjects")
  String create(Model model, @PathVariable("caseId") Long caseId,
      @RequestBody String formData) {
    Case c = caseRepo.findOne(caseId);
    Subject s = new Subject();

    s.setJsonData(formData);
    subjectRepo.save(s);

    c.getSubjects().add(s);
    caseRepo.save(c);

    model.addAttribute("case", c);
    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItems", c.getSubjects());
    return "subjects/list :: list";
  }

  @PostMapping(path = "/cases/{caseId}/subjects/{id}")
  String update(Model model, @PathVariable("caseId") Long caseId,
      @PathVariable("id") Long id, @RequestBody String formData) {
    Subject subject = subjectRepo.findOne(id);
    subject.setJsonData(formData);
    subjectRepo.save(subject);

    Case c = caseRepo.findOne(caseId);
    List<Subject> subjects = c.getSubjects();

    model.addAttribute("case", c);
    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItems", subjects);
    return "subjects/list :: list";
  }

  @GetMapping(path = "/cases/{caseId}/subjects/{id}")
  String show(Model model, @PathVariable("caseId") Long caseId,
      @PathVariable("id") Long id) {
    Subject subject = subjectRepo.findOne(id);

    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItem", subject);
    return "subjects/show :: show";
  }

  @GetMapping(path = "/cases/{caseId}/subjects/{id}/edit")
  String edit(Model model, @PathVariable("caseId") Long caseId,
      @PathVariable("id") Long id) {
    Subject subject = subjectRepo.findOne(id);

    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects/" + id);
    model.addAttribute("jsfItem", subject);
    return "subjects/edit :: edit";
  }

  @GetMapping(path = "/cases/{caseId}/subjects/{id}/status/{status}")
  String alterStatus(@PathVariable("caseId") Long caseId,
      @PathVariable("id") Long id, @PathVariable("status") String status) {
    Subject subject = subjectRepo.findOne(id);
    subject.setStatus(Subject.Status.fromString(status));
    subjectRepo.save(subject);

    return "redirect:/cases/" + caseId + "/subjects/index";
  }

  @PostMapping(path = "/cases/{caseId}/subjects/batchdating")
  String batchDating(Model model, @PathVariable("caseId") Long caseId,
      @RequestParam("subjectDateType") String subjectDateType,
      @RequestParam("subjectDate") String subjectDate,
      @RequestParam("subjectIds[]") List<Long> subjectIds) {
    Case c = caseRepo.findOne(caseId);
    List<Subject> subjects = c.getSubjects();

    subjects.forEach(s -> {
      if (subjectIds.contains(s.getId())) {
        String jsonData = s.getJsonData();
        JsonObject jo = Json.parse(jsonData).asObject();
        jo.set(subjectDateType, subjectDate);
        s.setJsonData(jo.toString());
        subjectRepo.save(s);
      }
    });

    model.addAttribute("case", c);
    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItems", subjects);
    return "subjects/index";
  }

  @PostMapping(path = "/cases/{caseId}/subjects/index")
  String batchFile(Model model, @PathVariable("caseId") Long caseId,
      @RequestParam("subjectFile") MultipartFile file) {
    Case c = caseRepo.findOne(caseId);

    ExcelSubjects es = new ExcelSubjects(file);
    if (es.getErrorMessage() == null) {
      subjectRepo.save(es.getSubjects());
      c.getSubjects().addAll(es.getSubjects());
      caseRepo.save(c);
    } else {
      model.addAttribute("message", es.getErrorMessage());
    }

    model.addAttribute("case", c);
    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItems", c.getSubjects());
    return "subjects/index";
  }

  @GetMapping("/subjects/query/{nationalId}")
  Patient searchPatient(Model model,
      @PathVariable("nationalId") String nationalId) {
    return tsghApi.serachPatientById(nationalId);
  }

}
