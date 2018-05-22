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

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.wmw.crc.manager.model.Case;
import com.wmw.crc.manager.model.Case.Status;
import com.wmw.crc.manager.repository.CaseRepository;
import com.wmw.crc.manager.service.KeycloakService;

@Controller
public class CaseOperationController {

  @Autowired
  CaseRepository caseRepo;

  @Autowired
  KeycloakService keycloak;

  @RequestMapping(path = "/cases/{id}/assignment", method = GET)
  String assignment(Model model, @PathVariable("id") Long id) {
    model.addAttribute("users", keycloak.getNormalUsers());
    model.addAttribute("case", caseRepo.findOne(id));
    return "cases/assignment/index";
  }

  @RequestMapping(path = "/cases/{id}/assignment", method = POST)
  String assign(@PathVariable("id") Long id,
      @RequestParam("username") String username) {
    Case kase = caseRepo.findOne(id);
    kase.setOwner(username);
    kase.setStatus(Status.EXEC);
    caseRepo.save(kase);

    return "redirect:/cases/index?new";
  }

  @RequestMapping(path = "/cases/{id}/permission", method = GET)
  String permission(Model model, @PathVariable("id") Long id) {
    model.addAttribute("users", keycloak.getNormalUsers());
    model.addAttribute("case", caseRepo.findOne(id));
    return "cases/permission/index";
  }

  @RequestMapping(path = "/cases/{id}/permission/managers", method = POST)
  String addManager(Model model, @PathVariable("id") Long id,
      @RequestBody Map<String, Object> body) {
    Case kase = caseRepo.findOne(id);
    kase.getManagers().add(body.get("manager").toString());
    caseRepo.save(kase);

    model.addAttribute("case", kase);
    return "cases/permission/manager-list :: manager-list";
  }

  @RequestMapping(path = "/cases/{id}/permission/managers", method = DELETE)
  String removeManager(Model model, @PathVariable("id") Long id,
      @RequestParam("manager") String manager) {
    Case kase = caseRepo.findOne(id);
    kase.getManagers().remove(manager);
    caseRepo.save(kase);

    model.addAttribute("case", kase);
    return "cases/permission/manager-list :: manager-list";
  }

  @RequestMapping(path = "/cases/{id}/permission/editors", method = POST)
  String addEditor(Model model, @PathVariable("id") Long id,
      @RequestBody Map<String, Object> body) {
    Case kase = caseRepo.findOne(id);
    kase.getEditors().add(body.get("editor").toString());
    caseRepo.save(kase);

    model.addAttribute("case", kase);
    return "cases/permission/editor-list :: editor-list";
  }

  @RequestMapping(path = "/cases/{id}/permission/editors", method = DELETE)
  String removeEditor(Model model, @PathVariable("id") Long id,
      @RequestParam("editor") String editor) {
    Case kase = caseRepo.findOne(id);
    kase.getEditors().remove(editor);
    caseRepo.save(kase);

    model.addAttribute("case", kase);
    return "cases/permission/editor-list :: editor-list";
  }

  @RequestMapping(path = "/cases/{id}/permission/viewers", method = POST)
  String addViewer(Model model, @PathVariable("id") Long id,
      @RequestBody Map<String, Object> body) {
    Case kase = caseRepo.findOne(id);
    kase.getViewers().add(body.get("viewer").toString());
    caseRepo.save(kase);

    model.addAttribute("case", kase);
    return "cases/permission/viewer-list :: viewer-list";
  }

  @RequestMapping(path = "/cases/{id}/permission/viewers", method = DELETE)
  String removeViewerr(Model model, @PathVariable("id") Long id,
      @RequestParam("viewer") String viewer) {
    Case kase = caseRepo.findOne(id);
    kase.getViewers().remove(viewer);
    caseRepo.save(kase);

    model.addAttribute("case", kase);
    return "cases/permission/viewer-list :: viewer-list";
  }

}
