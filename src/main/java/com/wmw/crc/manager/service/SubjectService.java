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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.wmw.crc.manager.model.CaseStudy;
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

  public void batchCreate(CaseStudy c, ExcelSubjects es) {
    List<Subject> subjects = subjectRepo.findAllByCaseStudy(c);

    List<String> nationalIds =
        Ruby.Array.of(subjects).map(Subject::getNationalId).toList();

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

  public Subject updateOrCreateSubject(CaseStudy cs, Subject subject) {
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

  public Subject createSubject(CaseStudy cs, Subject subject) {
    List<Subject> subjects = subjectRepo.findAllByCaseStudy(cs);

    Optional<Subject> opt = subjects.stream().filter(s -> {
      return Objects.equals(s.getNationalId(), subject.getNationalId())
          && s.getStatus() != Status.DROPPED;
    }).findAny();

    if (!opt.isPresent()) {
      subject.setCaseStudy(cs);
      subjectRepo.save(subject);

      return subject;
    }

    return null;
  }

  public Subject updateSubject(CaseStudy cs, Subject subject,
      JsonNode formData) {
    List<Subject> subjects = subjectRepo.findAllByCaseStudy(cs);

    String nationalId = subject.getNationalId();

    JsonNode oldFormData = subject.getFormData();
    subject.setFormData(formData);

    if (Objects.equals(nationalId, subject.getNationalId())) {
      subjectRepo.save(subject);

      return subject;
    }

    int count = Ruby.Array.of(subjects).count(s -> {
      return Objects.equals(s.getNationalId(), subject.getNationalId())
          && s.getStatus() != Status.DROPPED;
    });

    if (count == 0) {
      subjectRepo.save(subject);

      return subject;
    }

    subject.setFormData(oldFormData);
    return null;
  }

  public boolean secureDropoutDate(Subject subject, JsonNode formData) {
    if (subject == null) return true;

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

}
