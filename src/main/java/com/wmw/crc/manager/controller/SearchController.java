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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.wnameless.json.flattener.JsonFlattener;
import com.wmw.crc.manager.model.Case;
import com.wmw.crc.manager.model.Criterion;
import com.wmw.crc.manager.repository.CaseRepository;

@Controller
public class SearchController {

  @Autowired
  CaseRepository caseRepo;

  @GetMapping("/search/index")
  String index(Model model) {
    model.addAttribute("propertyTitles", new Case().propertyTitles());
    return "search/index";
  }

  @PostMapping("/search")
  String search(Model model, Authentication auth,
      @RequestBody List<Criterion> criteria) {
    List<Case> cases = caseRepo.findByUserAndCriteria(auth, criteria);

    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("jsfItems", cases);
    return "search/result :: result";
  }

  @GetMapping("/download/case/{id}")
  @ResponseBody
  HttpEntity<byte[]> download(@PathVariable("id") Long id) {
    Case kase = caseRepo.findOne(id);

    byte[] documentBody = JsonFlattener.flatten(kase.getJsonData()).getBytes();

    HttpHeaders header = new HttpHeaders();
    header.setContentType(MediaType.APPLICATION_JSON_UTF8);
    header.set(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=" + kase.getIrbNumber() + ".json");
    header.setContentLength(documentBody.length);

    return new HttpEntity<byte[]>(documentBody, header);
  }

}
