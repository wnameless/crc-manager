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

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.wmw.crc.manager.model.Case;
import com.wmw.crc.manager.repository.CaseRepository;

@Controller
public class CaseController {

  @ModelAttribute("CASES_STATUS")
  Case.Status currentStatus(HttpSession session,
      @RequestParam Map<String, String> allRequestParams) {
    if (allRequestParams.containsKey("new")) {
      session.setAttribute("CASES_STATUS", Case.Status.NEW);
      return Case.Status.NEW;
    } else if (allRequestParams.containsKey("exec")) {
      session.setAttribute("CASES_STATUS", Case.Status.EXEC);
      return Case.Status.EXEC;
    } else if (allRequestParams.containsKey("end")) {
      session.setAttribute("CASES_STATUS", Case.Status.END);
      return Case.Status.END;
    } else if (allRequestParams.containsKey("none")) {
      session.setAttribute("CASES_STATUS", Case.Status.NONE);
      return Case.Status.NONE;
    }

    return Case.Status.EXEC;
  }

  @Autowired
  CaseRepository caseRepo;

  @PreAuthorize("@perm.isUser()")
  @GetMapping("/cases/index")
  String index(HttpSession session, Authentication auth, Model model) {
    Iterable<Case> cases = getCasesBySession(auth, session);

    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("jsfItems", cases);
    return "cases/index";
  }

  @PreAuthorize("@perm.isUser()")
  @GetMapping("/cases")
  String list(HttpSession session, Authentication auth, Model model) {
    Iterable<Case> cases = getCasesBySession(auth, session);

    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("jsfItems", cases);
    return "cases/list :: list";
  }

  @PreAuthorize("@perm.canRead(#id)")
  @GetMapping("/cases/{id}")
  String show(@PathVariable("id") Long id, Model model) {
    Case c = caseRepo.getOne(id);

    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("jsfItem", c);
    return "cases/show :: show";
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @GetMapping("/cases/{id}/edit")
  String edit(Model model, @PathVariable("id") Long id) {
    Case c = caseRepo.getOne(id);

    model.addAttribute("jsfPath", "/cases/" + id);
    model.addAttribute("jsfItem", c);
    return "cases/edit :: edit";
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @PostMapping("/cases/{id}")
  String save(HttpSession session, Authentication auth, Model model,
      @PathVariable("id") Long id, @RequestBody String formData) {
    Case c = caseRepo.getOne(id);
    c.setFormData(formData);
    caseRepo.save(c);

    Iterable<Case> cases = getCasesBySession(auth, session);
    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("jsfItems", cases);
    return "cases/list :: list";
  }

  @PreAuthorize("@perm.canDelete()")
  @GetMapping("/cases/{id}/delete")
  String delete(HttpSession session, Authentication auth, Model model,
      @PathVariable("id") Long id) {
    Case c = caseRepo.getOne(id);
    caseRepo.delete(c);

    Iterable<Case> cases = getCasesBySession(auth, session);
    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("jsfItems", cases);
    return "redirect:/cases/index";
  }

  private Iterable<Case> getCasesBySession(Authentication auth,
      HttpSession session) {
    return caseRepo.findByUserAndStatus(auth,
        (Case.Status) session.getAttribute("CASES_STATUS"));
  }

}
