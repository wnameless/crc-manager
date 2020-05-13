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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import com.wmw.crc.manager.service.JsonDataExportService;

import net.sf.rubycollect4j.Ruby;

@Controller
public class SearchController {

  @Autowired
  CaseStudyRepository caseRepo;

  @Autowired
  SubjectRepository subjectRepo;

  @Autowired
  JsonDataExportService dataExport;

  @PreAuthorize("@perm.isUser()")
  @GetMapping("/search/index")
  String index(Model model) {
    model.addAttribute("casePropertyTitles",
        ReactJsonSchemaFormUtils.propertyTitles(new CaseStudy()));
    model.addAttribute("subjectPropertyTitles",
        ReactJsonSchemaFormUtils.propertyTitles(new Subject()));
    return "search/index";
  }

  @PreAuthorize("@perm.isUser()")
  @PostMapping("/search")
  String search(Model model, Authentication auth,
      @RequestBody List<Criterion> criteria) {
    List<Criterion> caseCriterion =
        criteria.stream().filter(c -> Objects.equals(c.getType(), "CaseStudy"))
            .collect(Collectors.toList());
    List<Criterion> subjectCriterion =
        criteria.stream().filter(c -> Objects.equals(c.getType(), "Subject"))
            .collect(Collectors.toList());

    List<CaseStudy> casesBySubjectCrits = null;
    if (caseCriterion.isEmpty() && !subjectCriterion.isEmpty()) {
      Iterable<CaseStudy> readableCases = caseRepo.findAllByUser(auth);

      Iterable<Subject> casesBySubjects = subjectRepo
          .findByCaseStudiesAndCriteria(readableCases, subjectCriterion);
      casesBySubjectCrits =
          Ruby.Enumerator.of(casesBySubjects).map(s -> s.getCaseStudy());
    } else {
      List<CaseStudy> readableCases =
          caseRepo.findByUserAndCriteria(auth, caseCriterion);

      Iterable<Subject> casesBySubjects = subjectRepo
          .findByCaseStudiesAndCriteria(readableCases, subjectCriterion);
      casesBySubjectCrits =
          Ruby.Enumerator.of(casesBySubjects).map(s -> s.getCaseStudy());

      casesBySubjectCrits.addAll(readableCases);
    }

    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("jsfItems",
        Ruby.Array.copyOf(casesBySubjectCrits).uniq().toList());
    return "search/result :: result";
  }

  @PreAuthorize("@perm.canRead(#id)")
  @GetMapping("/download/case/{id}")
  @ResponseBody
  HttpEntity<byte[]> download(@PathVariable("id") Long id, Locale locale)
      throws IOException {
    CaseStudy kase = caseRepo.findById(id).get();

    Workbook wb = dataExport.toExcel(kase, locale);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    wb.write(bos);
    wb.close();

    byte[] documentBody = bos.toByteArray();

    HttpHeaders header = new HttpHeaders();
    header.setContentType(MediaType.valueOf(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    header.set(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=" + kase.getIrbNumber() + ".xlsx");
    header.setContentLength(documentBody.length);

    return new HttpEntity<byte[]>(documentBody, header);
  }

}
