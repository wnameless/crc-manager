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

import static com.wmw.crc.manager.CrcManagerConfig.CASES_STATUS;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.service.CaseStudyService;;

@Controller
public class CaseStudyController {

  @Autowired
  CaseStudyRepository caseRepo;

  @Autowired
  CaseStudyService caseStudyService;

  @ModelAttribute("jsfPath")
  String initJsfPath() {
    return "/cases";
  }

  @ModelAttribute
  void initJsfItemPath(Model model,
      @PathVariable(name = "id", required = false) Long id) {
    if (id != null) model.addAttribute("jsfItemPath", "/cases/" + id);
  }

  @ModelAttribute
  void initModel(Model model,
      @PathVariable(name = "id", required = false) Long id) {
    if (id != null) {
      CaseStudy cs = caseRepo.findById(id).get();
      Map<String, Entry<String, Boolean>> files =
          caseStudyService.getFilesFromCaseStudy(cs);
      model.addAttribute("jsfItem", cs);
      model.addAttribute("files", files);
    }
  }

  @ModelAttribute
  void initPage(Model model, HttpSession session, Authentication auth,
      @RequestParam Map<String, String> allRequestParams,
      @PageableDefault(sort = "trialName") Pageable pageable,
      @RequestParam(required = false) String search) {
    if (allRequestParams.containsKey("new")) {
      session.setAttribute(CASES_STATUS, CaseStudy.Status.NEW);
    } else if (allRequestParams.containsKey("exec")) {
      session.setAttribute(CASES_STATUS, CaseStudy.Status.EXEC);
    } else if (allRequestParams.containsKey("end")) {
      session.setAttribute(CASES_STATUS, CaseStudy.Status.END);
    } else if (allRequestParams.containsKey("none")) {
      session.setAttribute(CASES_STATUS, CaseStudy.Status.NONE);
    }
    if (session.getAttribute(CASES_STATUS) == null) {
      session.setAttribute(CASES_STATUS, CaseStudy.Status.EXEC);
    }

    if (pageable == null) {
      if (session.getAttribute("pageable") == null) {
        pageable = PageRequest.of(0, 10, Sort.by("trialName"));
      } else {
        pageable = (Pageable) session.getAttribute("pageable");
      }
    }
    model.addAttribute("pageable", pageable);
    session.setAttribute("pageable", pageable);

    if (!allRequestParams.containsKey("search")) {
      if (session.getAttribute("search") != null) {
        search = (String) session.getAttribute("search");
      }
    }
    model.addAttribute("search", search);
    session.setAttribute("search", search);

    model.addAttribute("page",
        caseStudyService.getCasesBySession(auth, session, search, pageable));
  }

  @PreAuthorize("@perm.isUser()")
  @GetMapping(path = "/cases")
  String index(Model model) {
    return "cases/index";
  }

  @PreAuthorize("@perm.isUser()")
  @GetMapping(path = "/cases", produces = APPLICATION_JSON_VALUE)
  String indexJS(Model model, @RequestParam(required = false) String search) {
    return "cases/list :: partial";
  }

  @PreAuthorize("@perm.canRead(#id)")
  @GetMapping("/cases/{id}")
  String show(@PathVariable("id") Long id, Model model) {
    return "cases/show :: complete";
  }

  @PreAuthorize("@perm.canRead(#id)")
  @GetMapping(path = "/cases/{id}", produces = APPLICATION_JSON_VALUE)
  String showJS(@PathVariable("id") Long id, Model model) {
    return "cases/show :: partial";
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @GetMapping(path = "/cases/{id}/edit", produces = APPLICATION_JSON_VALUE)
  String editJS(Model model, @PathVariable("id") Long id) {
    return "cases/edit :: partial";
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @GetMapping("/cases/{id}/files/{fileId}")
  @ResponseBody
  HttpEntity<byte[]> downloadFile(Model model, @PathVariable("id") Long id,
      @PathVariable("fileId") String fileId) {
    CaseStudy c = (CaseStudy) model.getAttribute("jsfItem");
    return caseStudyService.getDownloadableFile(c, fileId);
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @PostMapping("/cases/{id}")
  String saveJS(Model model, @PathVariable("id") Long id,
      @RequestBody JsonNode formData) {
    CaseStudy c = (CaseStudy) model.getAttribute("jsfItem");
    c.setFormData(formData);
    caseRepo.save(c);
    return "cases/list :: partial";
  }

  @PreAuthorize("@perm.canDelete()")
  @GetMapping("/cases/{id}/delete")
  String delete(Model model, @PathVariable("id") Long id) {
    CaseStudy c = (CaseStudy) model.getAttribute("jsfItem");
    caseRepo.delete(c);
    return "redirect:/cases";
  }

}
