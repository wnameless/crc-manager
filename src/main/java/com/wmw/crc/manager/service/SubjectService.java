/*
 *
 * Copyright 2020 Wei-Ming Wu
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

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.wnameless.advancedoptional.AdvOpt;
import com.google.common.io.Resources;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.QSubject;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.model.Subject.Status;
import com.wmw.crc.manager.repository.SubjectRepository;
import com.wmw.crc.manager.util.ExcelSubjects;

import net.sf.rubycollect4j.Ruby;
import net.sf.rubycollect4j.RubyArray;

@Service
public class SubjectService {

  @Autowired
  SubjectRepository subjectRepo;

  @PersistenceContext
  EntityManager em;

  public AdvOpt<List<Subject>> findExecSubjects(String nationalId) {
    JPAQuery<Subject> query = new JPAQuery<>(em);
    QSubject qSubject = QSubject.subject;

    BooleanExpression isCaseStudyExec =
        qSubject.caseStudy.status.eq(CaseStudy.Status.EXEC);
    BooleanExpression eqNationalId = qSubject.nationalId.eq(nationalId);

    return AdvOpt.ofNullable(
        query.from(qSubject).where(isCaseStudyExec.and(eqNationalId)).fetch(),
        "No such active subject.");
  }

  public void batchCreate(CaseStudy c, ExcelSubjects es) {
    List<Subject> subjects = subjectRepo.findAllByCaseStudy(c);

    RubyArray<String> nationalIds =
        Ruby.Array.of(subjects).map(Subject::getNationalId);

    Map<Boolean, RubyArray<Subject>> existOrNot =
        Ruby.Array.of(es.getSubjects())
            .groupBy(s -> nationalIds.contains(s.getNationalId())).toMap();

    if (existOrNot.containsKey(true)) {
      for (Subject s : existOrNot.get(true)) {
        updateOrCreateSubject(c, s);
      }
    }

    if (existOrNot.containsKey(false)) {
      List<Subject> targets =
          existOrNot.get(false).uniq(s -> s.getNationalId()).toList();

      for (Subject s : targets) {
        s.setCaseStudy(c);
      }

      subjectRepo.saveAll(targets);
    }
  }

  private Subject updateOrCreateSubject(CaseStudy cs, Subject subject) {
    List<Subject> subjects = subjectRepo.findAllByCaseStudy(cs);

    Optional<Subject> opt = subjects.stream().filter(s -> {
      return Objects.equals(s.getNationalId(), subject.getNationalId())
          && s.getStatus() != Status.DROPPED;
    }).findAny();

    if (opt.isPresent()) {
      JsonNode formData = subject.getFormData();

      Subject target = opt.get();
      target.setFormData(formData);
      subjectRepo.save(target);

      return target;
    } else {
      subject.setCaseStudy(cs);
      subjectRepo.save(subject);

      return subject;
    }
  }

  public AdvOpt<Subject> createSubject(CaseStudy cs, Subject subject) {
    List<Subject> subjects = subjectRepo.findAllByCaseStudy(cs);

    Optional<Subject> opt = subjects.stream().filter(s -> {
      return Objects.equals(s.getNationalId(), subject.getNationalId())
          && s.getStatus() != Status.DROPPED;
    }).findAny();

    if (!opt.isPresent()) {
      subject.setCaseStudy(cs);
      subjectRepo.save(subject);

      return AdvOpt.of(subject);
    }

    return AdvOpt.ofNullable(null, "ctrl.subject.message.nationalid-existed");
  }

  public AdvOpt<Subject> updateSubject(Subject subject, JsonNode formData) {
    if (subject == null) {
      return AdvOpt.ofNullable(null, "ctrl.subject.message.subject-not-found");
    }

    boolean dropoutSafe = secureDropoutDate(subject, formData);
    if (!dropoutSafe) {
      return AdvOpt.ofNullable(null,
          "ctrl.subject.message.dropout-cannot-clear");
    }

    String oldNationalId = subject.getNationalId();
    JsonNode oldFormData = subject.getFormData();

    subject.setFormData(formData);
    if (Objects.equals(oldNationalId, subject.getNationalId())) {
      return AdvOpt.of(subjectRepo.save(subject));
    }

    long count = subjectRepo.findAllByCaseStudy(subject.getCaseStudy()).stream()
        .filter(s -> Objects.equals(s.getNationalId(), subject.getNationalId())
            && s.getStatus() != Status.DROPPED)
        .count();
    if (count == 0) {
      return AdvOpt.of(subjectRepo.save(subject));
    }

    subject.setFormData(oldFormData);
    return AdvOpt.ofNullable(null, "ctrl.subject.message.nationalid-existed");
  }

  private boolean secureDropoutDate(Subject subject, JsonNode formData) {
    JsonNode oldDropoutDate = subject.getFormData().get("dropoutDate");
    JsonNode newDropoutDate = formData.get("dropoutDate");

    if (oldDropoutDate == null || oldDropoutDate.asText().trim().isEmpty()) {
      return true;
    } else {
      if (newDropoutDate == null || newDropoutDate.asText().trim().isEmpty()) {
        return false;
      } else {
        return true;
      }
    }
  }

  public HttpEntity<byte[]> createDownloadableUploadExample()
      throws IOException {
    URL exampleUrl = Resources.getResource("examples/三總受試者名單範本.xlsx");

    byte[] dataByteArray = Resources.toByteArray(exampleUrl);

    HttpHeaders header = new HttpHeaders();
    header.setContentType(MediaType.valueOf(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    header.set(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=" + URLEncoder.encode("三總受試者名單範本.xlsx", "UTF-8"));
    header.setContentLength(dataByteArray.length);

    return new HttpEntity<byte[]>(dataByteArray, header);
  }

}
