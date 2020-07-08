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
import com.wmw.crc.manager.util.Criterion;
import com.wmw.crc.manager.util.JsonNodeUtils;

import net.sf.rubycollect4j.Ruby;
import net.sf.rubycollect4j.RubyArray;

@JaversSpringDataAuditable
@Repository
public interface CaseStudyRepository extends JpaRepository<CaseStudy, Long>,
    QuerydslPredicateExecutor<CaseStudy> {

  default List<CaseStudy> findAllByUserAndCriteria(Authentication auth,
      List<Criterion> criteria) {
    Iterable<CaseStudy> cases = findAllByUser(auth);

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
        String target = dataObj.get(key).isTextual()
            ? dataObj.get(key).textValue() : Ruby.Array
                .of(JsonNodeUtils.getStringifyValues(dataObj, key)).join();

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

  default Iterable<CaseStudy> findAllByUser(Authentication auth) {
    Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
    if (authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
        || authorities.contains(new SimpleGrantedAuthority("ROLE_SUPER"))) {
      return findAll();
    }

    String username = auth.getName();
    return findAllByOwnerEqualsOrManagersContainsOrEditorsContainsOrViewersContains(
        username, username, username, username);
  }

  default Iterable<CaseStudy> findAllByUserAndStatus(Authentication auth,
      CaseStudy.Status status) {
    Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
    if (authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
        || authorities.contains(new SimpleGrantedAuthority("ROLE_SUPER"))) {
      return findAllByStatus(status);
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

  default Page<CaseStudy> findAllByUserAndStatus(Authentication auth,
      CaseStudy.Status status, Pageable pageable) {
    Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
    if (authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
        || authorities.contains(new SimpleGrantedAuthority("ROLE_SUPER"))) {
      return findAllByStatus(status, pageable);
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

  default Page<CaseStudy> findAllByUserAndStatus(Authentication auth,
      CaseStudy.Status status, Pageable pageable, String search) {
    Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();

    String username = auth.getName();
    QCaseStudy qCase = QCaseStudy.caseStudy;
    BooleanExpression isStatus = qCase.status.eq(status);
    BooleanExpression isOwner = qCase.owner.eq(username);
    BooleanExpression hasManager = qCase.managers.contains(username);
    BooleanExpression hasEditor = qCase.editors.contains(username);
    BooleanExpression hasViewer = qCase.viewers.contains(username);

    BooleanExpression irbContains = qCase.irbNumber.contains(search);
    BooleanExpression adminNumContains = qCase.adminNumber.contains(search);
    BooleanExpression nameContains = qCase.trialName.contains(search);
    BooleanExpression piContains = qCase.piName.contains(search);
    BooleanExpression startDateContains =
        qCase.expectedStartDate.contains(search);
    BooleanExpression endDateContains = qCase.expectedEndDate.contains(search);
    BooleanExpression searchCond =
        irbContains.or(adminNumContains).or(nameContains).or(piContains)
            .or(startDateContains).or(endDateContains);

    if (authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
        || authorities.contains(new SimpleGrantedAuthority("ROLE_SUPER"))) {
      return findAll(searchCond.and(isStatus), pageable);
    }

    return findAll(searchCond.and(isStatus)
        .and(isOwner.or(hasManager).or(hasEditor).or(hasViewer)), pageable);
  }

  default Iterable<CaseStudy> findAllByOwnerEqualsOrManagersContainsOrEditorsContainsOrViewersContains(
      String username1, String username2, String username3, String username4) {
    QCaseStudy qCase = QCaseStudy.caseStudy;
    BooleanExpression isOwner = qCase.owner.eq(username1);
    BooleanExpression hasManager = qCase.managers.contains(username2);
    BooleanExpression hasEditor = qCase.editors.contains(username3);
    BooleanExpression hasViewer = qCase.viewers.contains(username4);
    return findAll(isOwner.or(hasManager).or(hasEditor).or(hasViewer));
  }

  List<CaseStudy> findAllByStatus(CaseStudy.Status status);

  Page<CaseStudy> findAllByStatus(CaseStudy.Status status, Pageable pageable);

  CaseStudy findByIrbNumber(String irbNumber);

}
