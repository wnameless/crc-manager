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

import static com.google.common.collect.Maps.newHashMap;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.Gson;
import com.wmw.crc.manager.account.model.User;
import com.wmw.crc.manager.account.repository.UserRepository;
import com.wmw.crc.manager.form.json.schema.FormJsonSchemaProvider;
import com.wmw.crc.manager.model.Case;
import com.wmw.crc.manager.repository.CaseRepository;

@Controller
public class CaseController {

  @Autowired
  FormJsonSchemaProvider schemaProvider;

  @Autowired
  UserRepository userRepo;

  @Autowired
  CaseRepository caseRepo;

  @RequestMapping(path = "/cases", method = GET)
  String getCases(Authentication auth, Model model,
      @RequestParam Map<String, String> requestParams) {
    User user = userRepo.findByEmail(auth.getName());
    List<Case> cases = caseRepo.findByUser(user);

    if (requestParams.keySet().contains("new")) {
      cases.removeIf(c -> c.isFormDone());
    } else if (requestParams.keySet().contains("supp1")) {
      cases.removeIf(c -> c.isFormSupplement1Done() || !c.isFormDone());
    } else if (requestParams.keySet().contains("supp2")) {
      cases.removeIf(c -> c.isFormSupplement2Done() || !c.isFormDone()
          || !c.isFormSupplement2Done());
    } else if (requestParams.keySet().contains("end")) {
      cases.removeIf(c -> !c.isFormDone() || !c.isFormSupplement1Done()
          || !c.isFormSupplement2Done());
    }
    model.addAttribute("cases", cases);

    return "/cases/index";
  }

  @RequestMapping(path = "/cases/{id}/form", method = GET)
  String showForm(@PathVariable("id") Long id, Model model) {
    Case c = caseRepo.findOne(id);

    schemaProvider.setSchemas(model.asMap());
    model.addAttribute("formData", c.getFormJsonData());

    model.addAttribute("formId", id);

    return "/cases/form";
  }

  @RequestMapping(path = "/cases/{id}/form", method = POST)
  String updateForm(@PathVariable("id") Long id,
      @RequestBody MultiValueMap<String, String> formData, Model model,
      ServletRequest request) {

    System.err.println("formData: " + newHashMap(request.getParameterMap()));

    Case c = caseRepo.findOne(id);
    c.setFormJsonData(new Gson().toJson(formData));
    c.setFormDone(true);
    caseRepo.save(c);

    return "redirect:/cases";
  }

  @RequestMapping(path = "/cases/{id}/supplement1", method = POST)
  String updateSupplement1(@RequestParam("id") Long id,
      @RequestBody String jsonData) {
    Case c = caseRepo.findOne(id);

    return "OK";
  }

  @RequestMapping(path = "/cases/{id}/supplement2", method = POST)
  String updateSupplement2(@RequestParam("id") Long id,
      @RequestBody String jsonData) {
    Case c = caseRepo.findOne(id);

    return "OK";
  }

}
