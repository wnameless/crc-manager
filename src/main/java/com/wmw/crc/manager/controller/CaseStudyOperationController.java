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

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.CaseStudy.Status;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.service.KeycloakService;

@Controller
public class CaseStudyOperationController {

  @Autowired
  CaseStudyRepository caseRepo;

  @Autowired
  KeycloakService keycloak;

  @Autowired
  MessageSource messageSource;

  @PreAuthorize("@perm.canManage(#id)")
  @RequestMapping(path = "/cases/{id}/status/{status}", method = GET)
  String alterStatus(@PathVariable("id") Long id,
      @PathVariable("status") String status) {
    CaseStudy c = caseRepo.getOne(id);
    String currentStatus = c.getStatus().toString().toLowerCase();
    c.setStatus(CaseStudy.Status.fromString(status));
    caseRepo.save(c);

    return "redirect:/cases?" + currentStatus;
  }

  @PreAuthorize("@perm.canAssign()")
  @RequestMapping(path = "/cases/{id}/assignment", method = GET)
  String assignment(Model model, @PathVariable("id") Long id) {
    model.addAttribute("users", keycloak.getNormalUsers());
    model.addAttribute("case", caseRepo.getOne(id));
    return "cases/assignment/index";
  }

  @PreAuthorize("@perm.canAssign()")
  @RequestMapping(path = "/cases/{id}/assignment", method = POST)
  String assign(@PathVariable("id") Long id,
      @RequestParam("username") String username) {
    CaseStudy kase = caseRepo.getOne(id);
    kase.setOwner(username);
    kase.setStatus(Status.EXEC);
    caseRepo.save(kase);

    return "redirect:/cases?new";
  }

  @PreAuthorize("@perm.canManage(#id)")
  @RequestMapping(path = "/cases/{id}/permission", method = GET)
  String permission(Model model, @PathVariable("id") Long id) {
    model.addAttribute("users", keycloak.getNormalUsers());
    model.addAttribute("case", caseRepo.getOne(id));
    return "cases/permission/index";
  }

  @PreAuthorize("@perm.canManage(#id)")
  @RequestMapping(path = "/cases/{id}/permission/managers", method = POST)
  String addManager(Model model, @PathVariable("id") Long id,
      @RequestBody Map<String, Object> body, Locale locale) {
    CaseStudy kase = caseRepo.getOne(id);
    String manager = body.get("manager").toString();
    kase.getManagers().add(manager);
    caseRepo.save(kase);

    model.addAttribute("message",
        messageSource.getMessage("ctrl.case.operation.message.manager-added",
            new Object[] { manager }, locale));
    model.addAttribute("case", kase);
    return "cases/permission/manager-list :: manager-list";
  }

  @PreAuthorize("@perm.canManage(#id)")
  @RequestMapping(path = "/cases/{id}/permission/managers", method = DELETE)
  String removeManager(Model model, @PathVariable("id") Long id,
      @RequestParam("manager") String manager, Locale locale) {
    CaseStudy kase = caseRepo.getOne(id);
    kase.getManagers().remove(manager);
    caseRepo.save(kase);

    model.addAttribute("message",
        messageSource.getMessage("ctrl.case.operation.message.manager-removed",
            new Object[] { manager }, locale));
    model.addAttribute("case", kase);
    return "cases/permission/manager-list :: manager-list";
  }

  @PreAuthorize("@perm.canManage(#id)")
  @RequestMapping(path = "/cases/{id}/permission/editors", method = POST)
  String addEditor(Model model, @PathVariable("id") Long id,
      @RequestBody Map<String, Object> body, Locale locale) {
    CaseStudy kase = caseRepo.getOne(id);
    String editor = body.get("editor").toString();
    kase.getEditors().add(editor);
    caseRepo.save(kase);

    model.addAttribute("message",
        messageSource.getMessage("ctrl.case.operation.message.editor-added",
            new Object[] { editor }, locale));
    model.addAttribute("case", kase);
    return "cases/permission/editor-list :: editor-list";
  }

  @PreAuthorize("@perm.canManage(#id)")
  @RequestMapping(path = "/cases/{id}/permission/editors", method = DELETE)
  String removeEditor(Model model, @PathVariable("id") Long id,
      @RequestParam("editor") String editor, Locale locale) {
    CaseStudy kase = caseRepo.getOne(id);
    kase.getEditors().remove(editor);
    caseRepo.save(kase);

    model.addAttribute("message",
        messageSource.getMessage("ctrl.case.operation.message.editor-removed",
            new Object[] { editor }, locale));
    model.addAttribute("case", kase);
    return "cases/permission/editor-list :: editor-list";
  }

  @PreAuthorize("@perm.canManage(#id)")
  @RequestMapping(path = "/cases/{id}/permission/viewers", method = POST)
  String addViewer(Model model, @PathVariable("id") Long id,
      @RequestBody Map<String, Object> body, Locale locale) {
    CaseStudy kase = caseRepo.getOne(id);
    String viewer = body.get("viewer").toString();
    kase.getViewers().add(viewer);
    caseRepo.save(kase);

    model.addAttribute("message",
        messageSource.getMessage("ctrl.case.operation.message.viewer-added",
            new Object[] { viewer }, locale));
    model.addAttribute("case", kase);
    return "cases/permission/viewer-list :: viewer-list";
  }

  @PreAuthorize("@perm.canManage(#id)")
  @RequestMapping(path = "/cases/{id}/permission/viewers", method = DELETE)
  String removeViewer(Model model, @PathVariable("id") Long id,
      @RequestParam("viewer") String viewer, Locale locale) {
    CaseStudy kase = caseRepo.getOne(id);
    kase.getViewers().remove(viewer);
    caseRepo.save(kase);

    model.addAttribute("message",
        messageSource.getMessage("ctrl.case.operation.message.viewer-removed",
            new Object[] { viewer }, locale));
    model.addAttribute("case", kase);
    return "cases/permission/viewer-list :: viewer-list";
  }

}
