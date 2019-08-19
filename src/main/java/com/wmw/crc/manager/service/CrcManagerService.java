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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Service;

import com.github.wnameless.advancedoptional.AdvOpt;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.wmw.crc.manager.model.CaseStudy.Status;
import com.wmw.crc.manager.model.QSubject;
import com.wmw.crc.manager.model.Subject;

@Service
public class CrcManagerService {

  @PersistenceContext
  EntityManager em;

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