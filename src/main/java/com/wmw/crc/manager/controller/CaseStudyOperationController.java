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

import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.CaseStudy.Status;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.service.I18nService;
import com.wmw.crc.manager.service.KeycloakService;

@Controller
public class CaseStudyOperationController {

  @Autowired
  CaseStudyRepository caseRepo;

  @Autowired
  KeycloakService keycloak;

  @Autowired
  I18nService i18n;

  @PreAuthorize("@perm.canManage(#id)")
  @GetMapping("/cases/{id}/status/{status}")
  String alterStatus(@PathVariable("id") Long id,
      @PathVariable("status") String status) {
    CaseStudy c = caseRepo.findById(id).get();
    String currentStatus = c.getStatus().toString().toLowerCase();
    c.setStatus(CaseStudy.Status.fromString(status));
    caseRepo.save(c);

    return "redirect:/cases?" + currentStatus;
  }

  @PreAuthorize("@perm.canAssign()")
  @GetMapping("/cases/{id}/assignment")
  String assignment(Model model, @PathVariable("id") Long id) {
    model.addAttribute("users", keycloak.getNormalUsers());
    model.addAttribute("case", caseRepo.findById(id).get());
    return "cases/assignment/index";
  }

  @PreAuthorize("@perm.canAssign()")
  @PostMapping("/cases/{id}/assignment")
  String assign(@PathVariable("id") Long id,
      @RequestParam("username") String username) {
    CaseStudy cs = caseRepo.findById(id).get();
    cs.setOwner(username);
    cs.setStatus(Status.EXEC);
    caseRepo.save(cs);

    return "redirect:/cases?new";
  }

  @PreAuthorize("@perm.canManage(#id)")
  @GetMapping("/cases/{id}/permission")
  String permission(Model model, @PathVariable("id") Long id) {
    model.addAttribute("users", keycloak.getNormalUsers());
    model.addAttribute("case", caseRepo.findById(id).get());
    return "cases/permission/index";
  }

  @PreAuthorize("@perm.canManage(#id)")
  @PostMapping("/cases/{id}/permission/managers")
  String addManager(Model model, @PathVariable("id") Long id,
      @RequestBody Map<String, Object> body, Locale locale) {
    CaseStudy cs = caseRepo.findById(id).get();
    String manager = body.get("manager").toString();
    cs.getManagers().add(manager);
    caseRepo.save(cs);

    model.addAttribute("message", i18n.caseManagerAdded(locale, manager));
    model.addAttribute("case", cs);
    return "cases/permission/manager-list :: manager-list";
  }

  @PreAuthorize("@perm.canManage(#id)")
  @DeleteMapping("/cases/{id}/permission/managers")
  String removeManager(Model model, @PathVariable("id") Long id,
      @RequestParam("manager") String manager, Locale locale) {
    CaseStudy kase = caseRepo.findById(id).get();
    kase.getManagers().remove(manager);
    caseRepo.save(kase);

    model.addAttribute("message", i18n.caseManagerRemoved(locale, manager));
    model.addAttribute("case", kase);
    return "cases/permission/manager-list :: manager-list";
  }

  @PreAuthorize("@perm.canManage(#id)")
  @PostMapping("/cases/{id}/permission/editors")
  String addEditor(Model model, @PathVariable("id") Long id,
      @RequestBody Map<String, Object> body, Locale locale) {
    CaseStudy kase = caseRepo.findById(id).get();
    String editor = body.get("editor").toString();
    kase.getEditors().add(editor);
    caseRepo.save(kase);

    model.addAttribute("message", i18n.caseEditorAdded(locale, editor));
    model.addAttribute("case", kase);
    return "cases/permission/editor-list :: editor-list";
  }

  @PreAuthorize("@perm.canManage(#id)")
  @DeleteMapping("/cases/{id}/permission/editors")
  String removeEditor(Model model, @PathVariable("id") Long id,
      @RequestParam("editor") String editor, Locale locale) {
    CaseStudy kase = caseRepo.findById(id).get();
    kase.getEditors().remove(editor);
    caseRepo.save(kase);

    model.addAttribute("message", i18n.caseEditorRemoved(locale, editor));
    model.addAttribute("case", kase);
    return "cases/permission/editor-list :: editor-list";
  }

  @PreAuthorize("@perm.canManage(#id)")
  @PostMapping("/cases/{id}/permission/viewers")
  String addViewer(Model model, @PathVariable("id") Long id,
      @RequestBody Map<String, Object> body, Locale locale) {
    CaseStudy cs = caseRepo.findById(id).get();
    String viewer = body.get("viewer").toString();
    cs.getViewers().add(viewer);
    caseRepo.save(cs);

    model.addAttribute("message", i18n.caseViewerAdded(locale, viewer));
    model.addAttribute("case", cs);
    return "cases/permission/viewer-list :: viewer-list";
  }

  @PreAuthorize("@perm.canManage(#id)")
  @DeleteMapping("/cases/{id}/permission/viewers")
  String removeViewer(Model model, @PathVariable("id") Long id,
      @RequestParam("viewer") String viewer, Locale locale) {
    CaseStudy cs = caseRepo.findById(id).get();
    cs.getViewers().remove(viewer);
    caseRepo.save(cs);

    model.addAttribute("message", i18n.caseViewerRemoved(locale, viewer));
    model.addAttribute("case", cs);
    return "cases/permission/viewer-list :: viewer-list";
  }

}
