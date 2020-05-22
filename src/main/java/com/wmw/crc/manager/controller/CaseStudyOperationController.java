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

import static com.wmw.crc.manager.model.RestfulModel.Names.CASE_STUDY;

import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.wnameless.spring.common.RestfulController;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.CaseStudy.Status;
import com.wmw.crc.manager.model.RestfulModel;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.service.I18nService;
import com.wmw.crc.manager.service.KeycloakService;

@RequestMapping("/" + CASE_STUDY + "/{id}")
@Controller
public class CaseStudyOperationController
    implements RestfulController<CaseStudy, Long, CaseStudyRepository> {

  @Autowired
  CaseStudyRepository caseRepo;
  @Autowired
  KeycloakService keycloak;
  @Autowired
  I18nService i18n;

  CaseStudy caseStudy;

  @ModelAttribute
  void init(@PathVariable(required = false) Long id) {
    caseStudy = getItem(id, new CaseStudy());
  }

  // @PreAuthorize("@perm.canManage(#id)")
  // @GetMapping("/status/{status}")
  // String alterStatus(@PathVariable Long id, @PathVariable String status) {
  // caseStudy.setStatus(Status.fromString(status, Status.NEW));
  // caseRepo.save(caseStudy);
  //
  // return "redirect:" + getRoute().getIndexPath();
  // }

  @PreAuthorize("@perm.canAssign()")
  @GetMapping("/assignment")
  String assignment(Model model) {
    model.addAttribute("users", keycloak.getNormalUsers());
    return "cases/assignment/index";
  }

  @PreAuthorize("@perm.canAssign()")
  @PostMapping("/assignment")
  String assign(@RequestParam String username) {
    caseStudy.setOwner(username);
    caseStudy.setStatus(Status.EXEC);
    caseRepo.save(caseStudy);

    return "redirect:" + getRoute().getIndexPath();
  }

  @PreAuthorize("@perm.canManage(#id)")
  @GetMapping("/permission")
  String permission(Model model, @PathVariable Long id) {
    model.addAttribute("users", keycloak.getNormalUsers());
    return "cases/permission/index";
  }

  @PreAuthorize("@perm.canManage(#id)")
  @PostMapping("/permission/managers")
  String addManager(Model model, @PathVariable Long id,
      @RequestBody Map<String, Object> body, Locale locale) {
    String manager = body.get("manager").toString();
    caseStudy.getManagers().add(manager);
    caseRepo.save(caseStudy);

    updateItem(model, caseStudy);
    model.addAttribute("message", i18n.caseManagerAdded(locale, manager));
    return "cases/permission/manager-list :: partial";
  }

  @PreAuthorize("@perm.canManage(#id)")
  @DeleteMapping("/permission/managers")
  String removeManager(Model model, @PathVariable Long id,
      @RequestParam("manager") String manager, Locale locale) {
    caseStudy.getManagers().remove(manager);
    caseRepo.save(caseStudy);

    updateItem(model, caseStudy);
    model.addAttribute("message", i18n.caseManagerRemoved(locale, manager));
    return "cases/permission/manager-list :: partial";
  }

  @PreAuthorize("@perm.canManage(#id)")
  @PostMapping("/permission/editors")
  String addEditor(Model model, @PathVariable Long id,
      @RequestBody Map<String, Object> body, Locale locale) {
    String editor = body.get("editor").toString();
    caseStudy.getEditors().add(editor);
    caseRepo.save(caseStudy);

    updateItem(model, caseStudy);
    model.addAttribute("message", i18n.caseEditorAdded(locale, editor));
    return "cases/permission/editor-list :: partial";
  }

  @PreAuthorize("@perm.canManage(#id)")
  @DeleteMapping("/permission/editors")
  String removeEditor(Model model, @PathVariable("id") Long id,
      @RequestParam("editor") String editor, Locale locale) {
    caseStudy.getEditors().remove(editor);
    caseRepo.save(caseStudy);

    updateItem(model, caseStudy);
    model.addAttribute("message", i18n.caseEditorRemoved(locale, editor));
    return "cases/permission/editor-list :: partial";
  }

  @PreAuthorize("@perm.canManage(#id)")
  @PostMapping("/permission/viewers")
  String addViewer(Model model, @PathVariable Long id,
      @RequestBody Map<String, Object> body, Locale locale) {
    String viewer = body.get("viewer").toString();
    caseStudy.getViewers().add(viewer);
    caseRepo.save(caseStudy);

    updateItem(model, caseStudy);
    model.addAttribute("message", i18n.caseViewerAdded(locale, viewer));
    return "cases/permission/viewer-list :: partial";
  }

  @PreAuthorize("@perm.canManage(#id)")
  @DeleteMapping("/permission/viewers")
  String removeViewer(Model model, @PathVariable Long id,
      @RequestParam("viewer") String viewer, Locale locale) {
    caseStudy.getViewers().remove(viewer);
    caseRepo.save(caseStudy);

    updateItem(model, caseStudy);
    model.addAttribute("message", i18n.caseViewerRemoved(locale, viewer));
    return "cases/permission/viewer-list :: partial";
  }

  @Override
  public RestfulModel getRoute() {
    return RestfulModel.CASE_STUDY;
  }

  @Override
  public CaseStudyRepository getRepository() {
    return caseRepo;
  }

}
