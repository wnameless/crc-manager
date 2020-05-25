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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.wnameless.spring.react.ReactJsonSchemaFormUtils;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.model.form.Criterion;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.repository.SubjectRepository;
import com.wmw.crc.manager.service.CaseStudyService;

@Controller
public class SearchController {

  @Autowired
  CaseStudyRepository caseStudyRepo;
  @Autowired
  SubjectRepository subjectRepo;
  @Autowired
  CaseStudyService caseStudyService;

  @PreAuthorize("@perm.isUser()")
  @GetMapping("/search")
  String index(Model model) {
    model.addAttribute("casePropertyTitles",
        ReactJsonSchemaFormUtils.propertyTitles(new CaseStudy()));
    model.addAttribute("subjectPropertyTitles",
        ReactJsonSchemaFormUtils.propertyTitles(new Subject()));
    return "search/index";
  }

  @PreAuthorize("@perm.isUser()")
  @PostMapping(path = "/search", produces = APPLICATION_JSON_VALUE)
  String searchJS(Model model, Authentication auth,
      @RequestBody List<Criterion> criteria) {
    List<CaseStudy> targets =
        caseStudyService.searchCaseStudies(auth, criteria);

    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("jsfItems", targets);
    return "search/result :: result";
  }

  @PreAuthorize("@perm.canRead(#id)")
  @GetMapping("/download/cases/{id}")
  @ResponseBody
  HttpEntity<byte[]> download(@PathVariable Long id, Locale locale)
      throws IOException {
    HttpEntity<byte[]> excel =
        caseStudyService.createDownloadableExcelCaseStudy(id, locale);

    return excel;
  }

}
