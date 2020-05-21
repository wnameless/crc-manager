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

import static com.github.wnameless.spring.common.ControllerHelpers.initPageable;
import static com.github.wnameless.spring.common.ControllerHelpers.initParam;
import static com.github.wnameless.spring.common.ControllerHelpers.initParamWithDefault;
import static com.wmw.crc.manager.model.RestfulModel.Names.CASE_STUDY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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
import com.github.wnameless.spring.common.RestfulController;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.CaseStudy.Status;
import com.wmw.crc.manager.model.RestfulModel;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.service.CaseStudyService;;

@RequestMapping("/" + CASE_STUDY)
@Controller
public class CaseStudyController implements
    RestfulController<CaseStudy, Long, CaseStudyRepository, RestfulModel> {

  @Autowired
  CaseStudyRepository caseRepo;

  @Autowired
  CaseStudyService caseService;

  CaseStudy c;
  CaseStudy.Status status;

  Authentication auth;
  Model model;

  @ModelAttribute
  void init(Model model, HttpSession session, Authentication auth,
      @PathVariable(required = false) Long id,
      @RequestParam(required = false) String status) {
    this.auth = auth;
    this.model = model;
    c = getItem(id, new CaseStudy());
    if (id != null) {
      this.model.addAttribute("files", caseService.getFilesFromCaseStudy(c));
    }
    this.status = (Status) initParamWithDefault("status",
        Status.fromString(status), Status.EXEC, this.model, session);
  }

  @ModelAttribute
  void initPageSlice(HttpSession session,
      @RequestParam Map<String, String> requestParams,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String page,
      @RequestParam(required = false) String size,
      @RequestParam(required = false) String sort) {
    search =
        (String) initParam(requestParams, "search", search, model, session);
    sort = (String) initParamWithDefault("sort", sort, "irbNumber", model,
        session);
    Pageable pageable = initPageable(page, size, sort, model, session);

    model.addAttribute("slice",
        caseService.getCasesByStatus(auth, status, pageable, search));
  }

  @PreAuthorize("@perm.isUser()")
  @GetMapping
  String index() {
    return "cases/list :: complete";
  }

  @PreAuthorize("@perm.isUser()")
  @GetMapping(produces = APPLICATION_JSON_VALUE)
  String indexJS() {
    return "cases/list :: partial";
  }

  @PreAuthorize("@perm.canRead(#id)")
  @GetMapping("/{id}")
  String show(@PathVariable Long id) {
    return "cases/show :: complete";
  }

  @PreAuthorize("@perm.canRead(#id)")
  @GetMapping(path = "/{id}", produces = APPLICATION_JSON_VALUE)
  String showJS(@PathVariable Long id) {
    return "cases/show :: partial";
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @GetMapping(path = "/{id}/edit", produces = APPLICATION_JSON_VALUE)
  String editJS(@PathVariable Long id) {
    return "cases/edit :: partial";
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @PostMapping("/{id}")
  String updateJS(@PathVariable Long id, @RequestBody JsonNode formData) {
    c.setFormData(formData);
    caseRepo.save(c);

    model.addAttribute("slice",
        caseService.getCasesByStatus(auth, status,
            (Pageable) model.getAttribute("pageable"),
            (String) model.getAttribute("search")));
    return "cases/list :: partial";
  }

  @PreAuthorize("@perm.canDelete()")
  @GetMapping("/{id}/delete")
  String delete(@PathVariable Long id) {
    if (c.getId() != null) caseRepo.delete(c);
    return "redirect:" + c.getIndexPath();
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @GetMapping("/{id}/files/{fileKey}")
  @ResponseBody
  HttpEntity<byte[]> downloadFile(@PathVariable Long id,
      @PathVariable String fileKey) {
    return caseService.getDownloadableFile(c, fileKey);
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
