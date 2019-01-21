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

import static com.google.common.collect.Maps.newHashMap;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import com.github.wnameless.spring.json.schema.form.ReactJsonSchemaForm;
import com.wmw.crc.manager.model.Case;
import com.wmw.crc.manager.repository.CaseRepository;

@Controller
public class CaseController {

  @Autowired
  CaseRepository caseRepo;

  @PreAuthorize("@perm.isUser()")
  @GetMapping("/cases/index")
  String index(HttpSession session, Authentication auth,
      @RequestParam Map<String, String> allRequestParams, Model model) {
    Iterable<Case> cases = getCasesBySession(auth, session, allRequestParams);

    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("jsfItems", cases);
    return "cases/index";
  }

  @PreAuthorize("@perm.isUser()")
  @GetMapping("/cases")
  String list(HttpSession session, Authentication auth,
      @RequestParam Map<String, String> allRequestParams, Model model) {
    Iterable<Case> cases = getCasesBySession(auth, session, allRequestParams);

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
    model.addAttribute("rjsf", ReactJsonSchemaForm.of(c, "/cases"));
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
    c.setJsonData(formData);
    caseRepo.save(c);

    Iterable<Case> cases = getCasesBySession(auth, session, newHashMap());
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

    Iterable<Case> cases = getCasesBySession(auth, session, newHashMap());
    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("jsfItems", cases);
    return "redirect:/cases/index";
  }

  private Iterable<Case> getCasesBySession(Authentication auth,
      HttpSession session, Map<String, String> allRequestParams) {
    Iterable<Case> cases;

    if (allRequestParams.containsKey("new")) {
      session.setAttribute("CASES_STATUS", "new");
      cases = caseRepo.findByUserAndStatus(auth, Case.Status.NEW);
    } else if (allRequestParams.containsKey("exec")) {
      cases = caseRepo.findByUserAndStatus(auth, Case.Status.EXEC);
      session.setAttribute("CASES_STATUS", "exec");
    } else if (allRequestParams.containsKey("end")) {
      cases = caseRepo.findByUserAndStatus(auth, Case.Status.END);
      session.setAttribute("CASES_STATUS", "end");
    } else if (allRequestParams.containsKey("none")) {
      cases = caseRepo.findByUserAndStatus(auth, Case.Status.NONE);
      session.setAttribute("CASES_STATUS", "none");
    } else {
      if (session.getAttribute("CASES_STATUS") == null) {
        session.setAttribute("CASES_STATUS", "exec");
      }
      switch ((String) session.getAttribute("CASES_STATUS")) {
        case "new":
          cases = caseRepo.findByUserAndStatus(auth, Case.Status.NEW);
          break;
        case "exec":
          cases = caseRepo.findByUserAndStatus(auth, Case.Status.EXEC);
          break;
        case "end":
          cases = caseRepo.findByUserAndStatus(auth, Case.Status.END);
          break;
        case "none":
          cases = caseRepo.findByUserAndStatus(auth, Case.Status.NONE);
          break;
        default:
          cases = caseRepo.findByUserAndStatus(auth, Case.Status.EXEC);
      }
    }

    return cases;
  }

}
