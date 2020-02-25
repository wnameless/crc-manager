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
package com.wmw.crc.manager.repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.QCaseStudy;
import com.wmw.crc.manager.model.form.Criterion;
import com.wmw.crc.manager.util.JsonNodeUtils;

import net.sf.rubycollect4j.Ruby;
import net.sf.rubycollect4j.RubyArray;

@JaversSpringDataAuditable
@Repository("caseRepo")
public interface CaseStudyRepository extends JpaRepository<CaseStudy, Long>,
    QuerydslPredicateExecutor<CaseStudy> {

  default Iterable<CaseStudy> findByUserAndCriteria(Authentication auth,
      List<Criterion> criteria) {
    Iterable<CaseStudy> cases = findByUser(auth);

    ListMultimap<String, Object> groupedCriteria = groupedCriteria(criteria);

    return Ruby.Array.copyOf(cases)
        .keepIf(kase -> isCriteriaMatch(kase.getFormData(), groupedCriteria))
        .toList();
  }

  default boolean isCriteriaMatch(JsonNode data,
      ListMultimap<String, Object> criteria) {
    if (!data.isObject()) return false;

    ObjectNode dataObj = (ObjectNode) data;

    boolean isMatch = true;
    for (String key : criteria.keySet()) {
      if (!dataObj.has(key)) return false;

      if (criteria.get(key).get(0) instanceof String) {
        String target =
            dataObj.get(key).isTextual() ? dataObj.get(key).textValue()
                : JsonNodeUtils.findFirstAsString(dataObj.get(key), "name");

        RubyArray<String> strings =
            Ruby.Array.of(criteria.get(key)).map(o -> o.toString());

        if (!strings.map(s -> target.contains(s)).contains(Boolean.TRUE)) {
          isMatch = false;
          break;
        }
      } else /* Number */ {
        BigDecimal target = dataObj.get(key).isNumber()
            ? new BigDecimal(dataObj.get(key).asText()) : BigDecimal.ZERO;
        RubyArray<Number> numbers =
            Ruby.Array.of(criteria.get(key)).map(o -> (Number) o);

        if (!numbers
            .map(n -> target.compareTo(new BigDecimal(n.toString())) == 0)
            .contains(Boolean.TRUE)) {
          isMatch = false;
          break;
        }
      }
    }

    return isMatch;
  }

  default ListMultimap<String, Object> groupedCriteria(
      List<Criterion> criteria) {
    ListMultimap<String, Object> c = ArrayListMultimap.create();
    for (Criterion criterion : criteria) {
      c.put(criterion.getKey(), criterion.getValue());
    }
    return c;
  }

  default Iterable<CaseStudy> findByUser(Authentication auth) {
    String username = auth.getName();

    if (username.equals("super") || username.equals("admin")) return findAll();

    return findByOwnerEqualsOrManagersContainsOrEditorsContainsOrViewersContains(
        username, username, username, username);
  }

  default Iterable<CaseStudy> findByUserAndStatus(Authentication auth,
      CaseStudy.Status status) {
    Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
    if (authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
        || authorities.contains(new SimpleGrantedAuthority("ROLE_SUPER"))) {
      return findByStatus(status);
    }

    String username = auth.getName();
    QCaseStudy qCase = QCaseStudy.caseStudy;
    BooleanExpression isStatus = qCase.status.eq(status);
    BooleanExpression isOwner = qCase.owner.eq(username);
    BooleanExpression hasManager = qCase.managers.contains(username);
    BooleanExpression hasEditor = qCase.editors.contains(username);
    BooleanExpression hasViewer = qCase.viewers.contains(username);
    return findAll(
        isStatus.and(isOwner.or(hasManager).or(hasEditor).or(hasViewer)));
  }

  default Page<CaseStudy> findByUserAndStatus(Authentication auth,
      CaseStudy.Status status, Pageable pageable) {
    Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
    if (authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
        || authorities.contains(new SimpleGrantedAuthority("ROLE_SUPER"))) {
      return findByStatus(status, pageable);
    }

    String username = auth.getName();
    QCaseStudy qCase = QCaseStudy.caseStudy;
    BooleanExpression isStatus = qCase.status.eq(status);
    BooleanExpression isOwner = qCase.owner.eq(username);
    BooleanExpression hasManager = qCase.managers.contains(username);
    BooleanExpression hasEditor = qCase.editors.contains(username);
    BooleanExpression hasViewer = qCase.viewers.contains(username);
    return findAll(
        isStatus.and(isOwner.or(hasManager).or(hasEditor).or(hasViewer)),
        pageable);
  }

  default Page<CaseStudy> findByUserAndStatus(Authentication auth,
      CaseStudy.Status status, String search, Pageable pageable) {
    Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();

    String username = auth.getName();
    QCaseStudy qCase = QCaseStudy.caseStudy;
    BooleanExpression isStatus = qCase.status.eq(status);
    BooleanExpression isOwner = qCase.owner.eq(username);
    BooleanExpression hasManager = qCase.managers.contains(username);
    BooleanExpression hasEditor = qCase.editors.contains(username);
    BooleanExpression hasViewer = qCase.viewers.contains(username);

    BooleanExpression irbContains = qCase.irbNumber.contains(search);
    BooleanExpression nameContains = qCase.trialName.contains(search);
    BooleanExpression piContains = qCase.piName.contains(search);
    BooleanExpression startDateContains =
        qCase.expectedStartDate.contains(search);
    BooleanExpression endDateContains = qCase.expectedEndDate.contains(search);
    BooleanExpression searchCond = irbContains.or(nameContains).or(piContains)
        .or(startDateContains).or(endDateContains);

    if (authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
        || authorities.contains(new SimpleGrantedAuthority("ROLE_SUPER"))) {
      return findAll(searchCond.and(isStatus), pageable);
    }

    return findAll(searchCond.and(isStatus)
        .and(isOwner.or(hasManager).or(hasEditor).or(hasViewer)), pageable);
  }

  default Iterable<CaseStudy> findByOwnerEqualsOrManagersContainsOrEditorsContainsOrViewersContains(
      String username1, String username2, String username3, String username4) {
    QCaseStudy qCase = QCaseStudy.caseStudy;
    BooleanExpression isOwner = qCase.owner.eq(username1);
    BooleanExpression hasManager = qCase.managers.contains(username2);
    BooleanExpression hasEditor = qCase.editors.contains(username3);
    BooleanExpression hasViewer = qCase.viewers.contains(username4);
    return findAll(isOwner.or(hasManager).or(hasEditor).or(hasViewer));
  }

  List<CaseStudy> findByStatus(CaseStudy.Status status);

  Page<CaseStudy> findByStatus(CaseStudy.Status status, Pageable pageable);

  CaseStudy findByIrbNumber(String irbNumber);

}
