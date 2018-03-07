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

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.wmw.crc.manager.model.Case;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.repository.CaseRepository;
import com.wmw.crc.manager.repository.SubjectRepository;

@Controller
public class SubjectController {

  @Autowired
  CaseRepository caseRepo;

  @Autowired
  SubjectRepository subjectRepo;

  @RequestMapping(path = "/cases/{caseId}/subjects/index", method = GET)
  String index(Model model, @PathVariable("caseId") Long caseId) {
    Case c = caseRepo.findOne(caseId);
    List<Subject> subjects = c.getSubjects();

    model.addAttribute("case", c);
    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItems", subjects);
    return "subjects/index";
  }

  @RequestMapping(path = "/cases/{caseId}/subjects", method = GET)
  String list(Model model, @PathVariable("caseId") Long caseId) {
    Case c = caseRepo.findOne(caseId);
    List<Subject> subjects = c.getSubjects();

    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItems", subjects);
    return "subjects/list :: list";
  }

  @RequestMapping(path = "/cases/{caseId}/subjects/new", method = GET)
  String New(Model model, @PathVariable("caseId") Long caseId) {
    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItem", new Subject());
    return "subjects/edit :: edit";
  }

  @RequestMapping(path = "/cases/{caseId}/subjects", method = POST)
  String create(Model model, @PathVariable("caseId") Long caseId,
      @RequestBody String formData) {
    Case c = caseRepo.findOne(caseId);
    Subject s = new Subject();

    s.setJsonData(formData);
    subjectRepo.save(s);

    c.getSubjects().add(s);
    caseRepo.save(c);

    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItems", c.getSubjects());
    return "subjects/list :: list";
  }

  @RequestMapping(path = "/cases/{caseId}/subjects/{id}", method = GET)
  String show(Model model, @PathVariable("caseId") Long caseId,
      @PathVariable("id") Long id) {
    Subject subject = subjectRepo.findOne(id);

    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects");
    model.addAttribute("jsfItem", subject);
    return "subjects/show :: show";
  }

  @RequestMapping(path = "/cases/{caseId}/subjects/{id}/edit", method = GET)
  String edit(Model model, @PathVariable("caseId") Long caseId,
      @PathVariable("id") Long id) {
    Subject subject = subjectRepo.findOne(id);

    model.addAttribute("jsfPath", "/cases/" + caseId + "/subjects/" + id);
    model.addAttribute("jsfItem", subject);
    return "subjects/edit :: edit";
  }

  @RequestMapping(path = "/cases/{caseId}/subjects/{id}", method = POST)
  String update(Model model, @PathVariable("caseId") Long caseId,
      @PathVariable("id") Long id, @RequestBody String formData) {
    Subject subject = subjectRepo.findOne(id);
    subject.setJsonData(formData);
    subjectRepo.save(subject);

    Case c = caseRepo.findOne(caseId);
    List<Subject> subjects = c.getSubjects();

    model.addAttribute("jsfPath", "/cases/{caseId}/subjects");
    model.addAttribute("jsfItems", subjects);
    return "subjects/list :: list";
  }

}
