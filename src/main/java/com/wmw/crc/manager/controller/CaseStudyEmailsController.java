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

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.wnameless.jpa.type.flattenedjson.FlattenedJsonTypeConfigurer;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.Emails;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.service.CaseStudyService;

@Controller
public class CaseStudyEmailsController {

  @Autowired
  CaseStudyRepository caseRepo;

  @Autowired
  CaseStudyService caseStudyService;

  @PreAuthorize("@perm.canWrite(#id)")
  @GetMapping("/cases/{id}/emails")
  String edit(@PathVariable("id") Long id, Model model) {
    CaseStudy c = caseRepo.findById(id).get();

    Emails emails = new Emails();

    ObjectNode on = FlattenedJsonTypeConfigurer.INSTANCE
        .getObjectMapperFactory().get().createObjectNode();
    ArrayNode an = on.putArray("listOfEmails");
    c.getEmails().stream().forEach(email -> {
      an.add(email);
    });
    emails.setFormData(on);

    model.addAttribute("case", c);
    model.addAttribute("jsfPath", "/cases/" + id + "/emails");
    model.addAttribute("jsfItem", emails);
    return "cases/emails/index";
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @PostMapping("/cases/{id}/emails")
  String save(@PathVariable("id") Long id, Model model,
      @RequestBody JsonNode formData) {
    CaseStudy c = caseRepo.findById(id).get();
    Map<String, Entry<String, Boolean>> files =
        caseStudyService.getFilesFromCaseStudy(c);

    c.getEmails().clear();
    JsonNode listOfEmails = formData.get("listOfEmails");
    for (int i = 0; i < listOfEmails.size(); i++) {
      c.getEmails().add(listOfEmails.get(i).asText());
    }

    caseRepo.save(c);

    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("jsfItem", c);
    model.addAttribute("files", files);
    return "cases/show :: show";
  }

}
