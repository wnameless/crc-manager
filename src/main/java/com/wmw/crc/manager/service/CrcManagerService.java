/*
 *
 * Copyright 2019 Wei-Ming Wu
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
package com.wmw.crc.manager.service;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.wnameless.advancedoptional.AdvOpt;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.CaseStudy.Status;
import com.wmw.crc.manager.model.QSubject;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.repository.CaseStudyRepository;

import net.sf.rubycollect4j.Ruby;

@Service
public class CrcManagerService {

  @Autowired
  CaseStudyRepository caseRepo;

  @PersistenceContext
  EntityManager em;

  public HttpEntity<byte[]> getDownloadableFile(CaseStudy cs, String fileId) {
    JsonNode formData = cs.getFormData();
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

  public Map<String, Entry<String, Boolean>> getFilesFromCaseStudy(
      CaseStudy cs) {
    Map<String, Entry<String, Boolean>> files = new LinkedHashMap<>();

    JsonNode schema = cs.getSchema();
    JsonNode formData = cs.getFormData();
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

    return files;
  }

  public Iterable<CaseStudy> getCasesBySession(Authentication auth,
      HttpSession session) {
    return caseRepo.findByUserAndStatus(auth,
        (CaseStudy.Status) session.getAttribute("CASES_STATUS"));
  }

  public Page<CaseStudy> getCasesBySession(Authentication auth,
      HttpSession session, Pageable pageable) {
    return caseRepo.findByUserAndStatus(auth,
        (CaseStudy.Status) session.getAttribute("CASES_STATUS"), pageable);
  }

  public Page<CaseStudy> getCasesBySession(Authentication auth,
      HttpSession session, String search, Pageable pageable) {
    if (search != null && !search.isEmpty()) {
      return caseRepo.findByUserAndStatus(auth,
          (CaseStudy.Status) session.getAttribute("CASES_STATUS"), search,
          pageable);
    }

    return getCasesBySession(auth, session, pageable);
  }

  public AdvOpt<List<Subject>> findExecSubjects(String nationalId) {
    JPAQuery<Subject> query = new JPAQuery<>(em);
    QSubject qSubject = QSubject.subject;

    BooleanExpression isCaseStudyExec =
        qSubject.caseStudy.status.eq(Status.EXEC);
    BooleanExpression eqNationalId = qSubject.nationalId.eq(nationalId);

    return AdvOpt.ofNullable(
        query.from(qSubject).where(isCaseStudyExec.and(eqNationalId)).fetch(),
        "No such active subject.");
  }

  public AdvOpt<List<Subject>> findNewSubjects(String nationalId) {
    JPAQuery<Subject> query = new JPAQuery<>(em);
    QSubject qSubject = QSubject.subject;

    BooleanExpression isCaseStudyExec =
        qSubject.caseStudy.status.eq(Status.NEW);
    BooleanExpression eqNationalId = qSubject.nationalId.eq(nationalId);

    return AdvOpt.ofNullable(
        query.from(qSubject).where(isCaseStudyExec.and(eqNationalId)).fetch(),
        "No such active subject.");
  }

}
