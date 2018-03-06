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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.gson.Gson;
import com.wmw.crc.manager.model.Case;
import com.wmw.crc.manager.repository.CaseRepository;

@Controller
public class CaseSupplementController {

  @Autowired
  CaseRepository caseRepo;

  @RequestMapping(path = "/cases/{id}/supplement2", method = GET)
  String showForm(@PathVariable("id") Long id, Model model) {
    Case c = caseRepo.findOne(id);

    model.asMap().put("jsonSchema", c.getSupplement1().getJsonSchema());
    model.asMap().put("jsonUiSchema", c.getSupplement1().getJsonUiSchema());
    model.addAttribute("jsonData", c.getSupplement1().getJsonData());
    model.addAttribute("formId", id);

    return "/cases/form";
  }

  @RequestMapping(path = "/cases/{id}/supplement2/save", method = POST)
  String saveForm(@PathVariable("id") Long id,
      @RequestBody MultiValueMap<String, Object> formData) {
    Case c = caseRepo.findOne(id);
    c.getSupplement1().setJsonData(new Gson().toJson(formData));
    caseRepo.save(c);

    return "redirect:/cases";
  }

  @RequestMapping(path = "/cases/{id}/supplement2", method = POST)
  String completeForm(@PathVariable("id") Long id,
      @RequestBody MultiValueMap<String, Object> formData) {
    Case c = caseRepo.findOne(id);
    c.getSupplement1().setJsonData(new Gson().toJson(formData));
    c.getSupplement1().setFormDone(true);
    caseRepo.save(c);

    return "redirect:/cases";
  }

}
