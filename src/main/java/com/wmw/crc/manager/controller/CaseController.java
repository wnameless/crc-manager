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

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.wmw.crc.manager.account.repository.UserRepository;
import com.wmw.crc.manager.model.Case;
import com.wmw.crc.manager.repository.CaseRepository;

@Controller
public class CaseController {

  @Autowired
  UserRepository userRepo;

  @Autowired
  CaseRepository caseRepo;

  private List<Case> getCasesBySession(HttpSession session,
      Map<String, String> allRequestParams) {
    List<Case> cases;

    if (allRequestParams.containsKey("new")) {
      session.setAttribute("CASES_STATUS", "new");
      cases = caseRepo.findByStatus(Case.Status.NEW);
    } else if (allRequestParams.containsKey("exec")) {
      cases = caseRepo.findByStatus(Case.Status.EXEC);
      session.setAttribute("CASES_STATUS", "exec");
    } else if (allRequestParams.containsKey("end")) {
      cases = caseRepo.findByStatus(Case.Status.END);
      session.setAttribute("CASES_STATUS", "end");
    } else if (allRequestParams.containsKey("none")) {
      cases = caseRepo.findByStatus(Case.Status.NONE);
      session.setAttribute("CASES_STATUS", "none");
    } else {
      if (session.getAttribute("CASES_STATUS") == null) {
        session.setAttribute("CASES_STATUS", "exec");
      }
      switch ((String) session.getAttribute("CASES_STATUS")) {
        case "new":
          cases = caseRepo.findByStatus(Case.Status.NEW);
          break;
        case "exec":
          cases = caseRepo.findByStatus(Case.Status.EXEC);
          break;
        case "end":
          cases = caseRepo.findByStatus(Case.Status.END);
          break;
        case "none":
          cases = caseRepo.findByStatus(Case.Status.NONE);
          break;
        default:
          cases = caseRepo.findByStatus(Case.Status.EXEC);
      }
    }

    return cases;
  }

  @RequestMapping(path = "/cases/index", method = GET)
  String index(HttpSession session, Authentication auth,
      @RequestParam Map<String, String> allRequestParams, Model model) {
    // User user = userRepo.findByEmail(auth.getName());
    // List<Case> cases = caseRepo.findByUser(user);

    List<Case> cases = getCasesBySession(session, allRequestParams);

    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("jsfItems", cases);
    return "cases/index";
  }

  @RequestMapping(path = "/cases", method = GET)
  String list(HttpSession session, Authentication auth,
      @RequestParam Map<String, String> allRequestParams, Model model) {
    List<Case> cases = getCasesBySession(session, allRequestParams);

    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("jsfItems", cases);
    return "cases/list :: list";
  }

  @RequestMapping(path = "/cases/{id}", method = GET)
  String show(@PathVariable("id") Long id, Model model) {
    Case c = caseRepo.findOne(id);

    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("jsfItem", c);
    return "cases/show :: show";
  }

  @RequestMapping(path = "/cases/{id}/edit", method = GET)
  String edit(Model model, @PathVariable("id") Long id) {
    Case c = caseRepo.findOne(id);

    model.addAttribute("jsfPath", "/cases/" + id);
    model.addAttribute("jsfItem", c);
    return "cases/edit :: edit";
  }

  @RequestMapping(path = "/cases/{id}", method = POST)
  String save(HttpSession session, Model model, @PathVariable("id") Long id,
      @RequestBody String formData) {
    Case c = caseRepo.findOne(id);
    c.setJsonData(formData);
    caseRepo.save(c);

    List<Case> cases = getCasesBySession(session, newHashMap());
    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("jsfItems", cases);
    return "cases/list :: list";
  }

  @RequestMapping(path = "/cases/{id}/status/{status}", method = GET)
  String alterStatus(@PathVariable("id") Long id,
      @PathVariable("status") String status) {
    Case c = caseRepo.findOne(id);
    String currentStatus = c.getStatus().toString().toLowerCase();
    c.setStatus(Case.Status.fromString(status));
    caseRepo.save(c);

    return "redirect:/cases/index?" + currentStatus;
  }

}
