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

import com.wmw.crc.manager.model.CRC;
import com.wmw.crc.manager.model.Case;
import com.wmw.crc.manager.repository.CRCRepository;
import com.wmw.crc.manager.repository.CaseRepository;

@Controller
public class CrcController {

  @Autowired
  CaseRepository caseRepo;

  @Autowired
  CRCRepository crcRepo;

  @RequestMapping(path = "/cases/{id}/crc", method = GET)
  String show(Model model, @PathVariable("id") Long id) {
    Case c = caseRepo.findOne(id);

    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("jsfItem", c.getCrc());
    return "crc/show :: show";
  }

  @RequestMapping(path = "/cases/{id}/crc/edit", method = GET)
  String edit(Model model, @PathVariable("id") Long id) {
    Case c = caseRepo.findOne(id);

    model.addAttribute("jsfPath", "/cases/" + id + "/crc");
    model.addAttribute("jsfItem", c.getCrc());
    return "crc/edit :: edit";
  }

  @RequestMapping(path = "/cases/{id}/crc", method = POST)
  String save(Model model, @PathVariable("id") Long id,
      @RequestBody String formData) {
    Case c = caseRepo.findOne(id);
    CRC crc = c.getCrc();
    crc.setJsonData(formData);
    crcRepo.save(crc);

    List<Case> cases = caseRepo.findByStatus(Case.Status.EXEC);
    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("jsfItems", cases);
    return "cases/list :: list";
  }

}
