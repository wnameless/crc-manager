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

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.service.CaseStudyService;

@Controller
public class CaseStudyController {

  @Autowired
  CaseStudyRepository caseRepo;

  @Autowired
  CaseStudyService caseStudyService;

  @PreAuthorize("@perm.isUser()")
  @RequestMapping(path = "/cases", method = { GET, PUT })
  String index(HttpServletRequest req, HttpSession session, Authentication auth,
      Model model, @PageableDefault(sort = "trialName") Pageable pageable,
      @RequestParam(required = false) String search) {
    Page<CaseStudy> page =
        caseStudyService.getCasesBySession(auth, session, search, pageable);

    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("pageable", pageable);
    model.addAttribute("page", page);
    model.addAttribute("search", search);
    return req.getMethod().equals("GET") ? "cases/index" : "cases/list :: list";
  }

  @PreAuthorize("@perm.canRead(#id)")
  @GetMapping("/cases/{id}")
  String show(@PathVariable("id") Long id, Model model) {
    CaseStudy c = caseRepo.findById(id).get();
    Map<String, Entry<String, Boolean>> files =
        caseStudyService.getFilesFromCaseStudy(c);

    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("jsfItem", c);
    model.addAttribute("files", files);
    return "cases/show :: show";
  }

  @PreAuthorize("@perm.canRead(#id)")
  @GetMapping("/cases/{id}/index")
  String showIndex(@PathVariable("id") Long id, Model model) {
    CaseStudy c = caseRepo.findById(id).get();
    Map<String, Entry<String, Boolean>> files =
        caseStudyService.getFilesFromCaseStudy(c);

    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("jsfItem", c);
    model.addAttribute("files", files);
    return "cases/show :: index";
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @GetMapping("/cases/{id}/edit")
  String edit(Model model, @PathVariable("id") Long id) {
    CaseStudy c = caseRepo.findById(id).get();

    Map<String, Entry<String, Boolean>> files =
        caseStudyService.getFilesFromCaseStudy(c);

    model.addAttribute("jsfPath", "/cases/" + id);
    model.addAttribute("jsfItem", c);
    model.addAttribute("files", files);
    return "cases/edit :: edit";
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @GetMapping("/cases/{id}/files/{fileId}")
  @ResponseBody
  HttpEntity<byte[]> downloadFile(Model model, @PathVariable("id") Long id,
      @PathVariable("fileId") String fileId) {
    CaseStudy c = caseRepo.findById(id).get();

    return caseStudyService.getDownloadableFile(c, fileId);
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @PostMapping("/cases/{id}")
  String save(HttpSession session, Authentication auth, Model model,
      @PathVariable("id") Long id, @RequestBody JsonNode formData,
      @PageableDefault(sort = "trialName") Pageable pageable)
      throws IOException {
    CaseStudy c = caseRepo.findById(id).get();
    c.setFormData(formData);
    caseRepo.save(c);

    Page<CaseStudy> page =
        caseStudyService.getCasesBySession(auth, session, pageable);

    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("pageable", pageable);
    model.addAttribute("page", page);
    return "cases/list :: list";
  }

  @PreAuthorize("@perm.canDelete()")
  @GetMapping("/cases/{id}/delete")
  String delete(HttpSession session, Authentication auth, Model model,
      @PathVariable("id") Long id,
      @PageableDefault(sort = "trialName") Pageable pageable) {
    CaseStudy c = caseRepo.findById(id).get();
    caseRepo.delete(c);

    Page<CaseStudy> page =
        caseStudyService.getCasesBySession(auth, session, pageable);

    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("pageable", pageable);
    model.addAttribute("page", page);
    return "redirect:/cases";
  }

  @ModelAttribute("CASES_STATUS")
  CaseStudy.Status currentStatus(HttpSession session,
      @RequestParam Map<String, String> allRequestParams) {
    if (allRequestParams.containsKey("new")) {
      session.setAttribute("CASES_STATUS", CaseStudy.Status.NEW);
      return CaseStudy.Status.NEW;
    } else if (allRequestParams.containsKey("exec")) {
      session.setAttribute("CASES_STATUS", CaseStudy.Status.EXEC);
      return CaseStudy.Status.EXEC;
    } else if (allRequestParams.containsKey("end")) {
      session.setAttribute("CASES_STATUS", CaseStudy.Status.END);
      return CaseStudy.Status.END;
    } else if (allRequestParams.containsKey("none")) {
      session.setAttribute("CASES_STATUS", CaseStudy.Status.NONE);
      return CaseStudy.Status.NONE;
    }

    if (session.getAttribute("CASES_STATUS") == null) {
      session.setAttribute("CASES_STATUS", CaseStudy.Status.EXEC);
    }
    return CaseStudy.Status.EXEC;
  }

}
