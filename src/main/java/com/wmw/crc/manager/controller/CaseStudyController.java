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
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.service.CrcManagerService;

import net.sf.rubycollect4j.Ruby;

@Controller
public class CaseStudyController {

  @Autowired
  CaseStudyRepository caseRepo;

  @Autowired
  CrcManagerService crcManagerService;

  @PreAuthorize("@perm.isUser()")
  @RequestMapping(path = "/cases", method = { GET, PUT })
  String index(HttpServletRequest req, HttpSession session, Authentication auth,
      Model model, @PageableDefault(sort = "trialName") Pageable pageable,
      @RequestParam(required = false) String search) {
    Page<CaseStudy> page;
    if (search != null && !search.isEmpty()) {
      page =
          crcManagerService.getCasesBySession(auth, session, search, pageable);
    } else {
      page = crcManagerService.getCasesBySession(auth, session, pageable);
    }

    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("pageable", pageable);
    model.addAttribute("page", page);
    model.addAttribute("search", search);
    if (req.getMethod().equals("GET")) {
      return "cases/index";
    } else {
      return "cases/list :: list";
    }
  }

  @PreAuthorize("@perm.canRead(#id)")
  @GetMapping("/cases/{id}")
  String show(@PathVariable("id") Long id, Model model) {
    CaseStudy c = caseRepo.getOne(id);

    Map<String, Entry<String, Boolean>> files = new LinkedHashMap<>();
    JsonNode schema = c.getSchema();
    JsonNode formData = c.getFormData();
    JsonNode fileNode =
        schema.get("properties").get("requiredFiles").get("properties");
    for (String fileId : Ruby.Array.copyOf(fileNode.fieldNames())) {
      String fileTitle = fileNode.get(fileId).get("title").textValue();
      JsonNode requiredFiles = formData.get("requiredFiles");
      JsonNode requiredFile = null;
      if (requiredFiles != null) {
        requiredFile = requiredFiles.get(fileId);
      }
      files.put(fileId,
          new AbstractMap.SimpleEntry<>(fileTitle, requiredFile != null));
    }

    model.addAttribute("jsfPath", "/cases");
    model.addAttribute("jsfItem", c);
    model.addAttribute("files", files);
    return "cases/show :: show";
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @GetMapping("/cases/{id}/edit")
  String edit(Model model, @PathVariable("id") Long id) {
    CaseStudy c = caseRepo.getOne(id);

    Map<String, Entry<String, Boolean>> files = new LinkedHashMap<>();
    JsonNode schema = c.getSchema();
    JsonNode formData = c.getFormData();
    JsonNode fileNode =
        schema.get("properties").get("requiredFiles").get("properties");
    for (String fileId : Ruby.Array.copyOf(fileNode.fieldNames())) {
      String fileTitle = fileNode.get(fileId).get("title").textValue();
      JsonNode requiredFiles = formData.get("requiredFiles");
      JsonNode requiredFile = null;
      if (requiredFiles != null) {
        requiredFile = requiredFiles.get(fileId);
      }
      files.put(fileId,
          new AbstractMap.SimpleEntry<>(fileTitle, requiredFile != null));
    }

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
    CaseStudy c = caseRepo.getOne(id);

    JsonNode formData = c.getFormData();
    String base64 = formData.get("requiredFiles").get(fileId).textValue();
    String[] base64Array = base64.split(";");

    String type = base64Array[0].substring(5);
    String name = base64Array[1].substring(5);
    String data = base64Array[2].substring(7);

    byte[] dataByteArray = Base64.decodeBase64(data.getBytes());

    HttpHeaders header = new HttpHeaders();
    header.setContentType(MediaType.valueOf(type));
    header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + name);
    header.setContentLength(dataByteArray.length);

    return new HttpEntity<byte[]>(dataByteArray, header);
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @PostMapping("/cases/{id}")
  String save(HttpSession session, Authentication auth, Model model,
      @PathVariable("id") Long id, @RequestBody String formData,
      @PageableDefault(sort = "trialName") Pageable pageable)
      throws IOException {
    ObjectMapper mapper = new ObjectMapper();

    CaseStudy c = caseRepo.getOne(id);
    c.setFormData(mapper.readTree(formData));
    caseRepo.save(c);

    Page<CaseStudy> page =
        crcManagerService.getCasesBySession(auth, session, pageable);

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
    CaseStudy c = caseRepo.getOne(id);
    caseRepo.delete(c);

    Page<CaseStudy> page =
        crcManagerService.getCasesBySession(auth, session, pageable);
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
