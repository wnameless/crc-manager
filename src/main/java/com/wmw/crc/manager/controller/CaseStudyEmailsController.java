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
import static com.wmw.crc.manager.model.RestfulModel.Names.CASE_STUDY;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.wnameless.jpa.type.flattenedjson.FlattenedJsonTypeConfigurer;
import com.github.wnameless.spring.common.ModelPolicy;
import com.github.wnameless.spring.common.RestfulController;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.Emails;
import com.wmw.crc.manager.model.RestfulModel;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.service.CaseStudyService;

@RequestMapping("/" + CASE_STUDY + "/{id}")
@Controller
public class CaseStudyEmailsController
    implements RestfulController<CaseStudy, Long, CaseStudyRepository> {

  @Autowired
  CaseStudyRepository caseStudyRepo;

  @Autowired
  CaseStudyService caseService;

  CaseStudy caseStudy;
  Emails emails;

  @Override
  public void configure(ModelPolicy<CaseStudy> policy) {
    policy.afterInit(item -> caseStudy = firstNonNull(item, new CaseStudy()));
  }

  @ModelAttribute
  void init(Model model, Locale locale,
      @PathVariable(required = false) Long id) {
    emails = new Emails();
    model.addAttribute("emails", emails);
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @GetMapping("/emails")
  String edit(Model model, @PathVariable Long id) {
    ObjectNode on = FlattenedJsonTypeConfigurer.INSTANCE
        .getObjectMapperFactory().get().createObjectNode();
    ArrayNode an = on.putArray("listOfEmails");
    caseStudy.getEmails().stream().forEach(email -> {
      an.add(email);
    });
    emails.setFormData(on);

    model.addAttribute("emails", emails);
    return "cases/emails/index";
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @PostMapping("/emails")
  String save(Model model, @PathVariable Long id,
      @RequestBody JsonNode formData) {
    caseStudy.getEmails().clear();
    JsonNode listOfEmails = formData.get("listOfEmails");
    for (int i = 0; i < listOfEmails.size(); i++) {
      caseStudy.getEmails().add(listOfEmails.get(i).asText());
    }
    caseStudyRepo.save(caseStudy);

    model.addAttribute("files", caseService.getFilesFromCaseStudy(caseStudy));
    return "cases/show :: partial";
  }

  @Override
  public RestfulModel getRoute() {
    return RestfulModel.CASE_STUDY;
  }

  @Override
  public CaseStudyRepository getRepository() {
    return caseStudyRepo;
  }

}
