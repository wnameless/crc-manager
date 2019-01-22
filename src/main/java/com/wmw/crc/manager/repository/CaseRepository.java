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
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Repository;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.wmw.crc.manager.model.Case;
import com.wmw.crc.manager.model.Criterion;
import com.wmw.crc.manager.model.QCase;
import com.wmw.crc.manager.util.MinimalJsonUtils;
import net.sf.rubycollect4j.Ruby;
import net.sf.rubycollect4j.RubyArray;

@Repository("caseRepo")
public interface CaseRepository
    extends JpaRepository<Case, Long>, QuerydslPredicateExecutor<Case> {

  default Iterable<Case> findByUserAndCriteria(Authentication auth,
      List<Criterion> criteria) {
    Iterable<Case> cases = findByUser(auth);

    ListMultimap<String, Object> groupedCriteria = groupedCriteria(criteria);
    Ruby.Array.copyOf(cases).keepIf(kase -> {
      JsonValue data = Json.parse(kase.getFormData());
      return isCriteriaMatch(data, groupedCriteria);
    });

    return cases;
  }

  default boolean isCriteriaMatch(JsonValue data,
      ListMultimap<String, Object> criteria) {
    if (!data.isObject()) return false;

    boolean isMatch = true;
    for (String key : criteria.keySet()) {
      if (!data.asObject().names().contains(key)) return false;

      if (criteria.get(key).get(0) instanceof String) {
        String target = data.asObject().get(key).isString()
            ? data.asObject().get(key).asString() : MinimalJsonUtils
                .findFirstAsString(data.asObject().get(key), "name");

        RubyArray<String> strings =
            Ruby.Array.of(criteria.get(key)).map(o -> o.toString());

        if (!strings.map(s -> target.contains(s)).contains(true)) {
          isMatch = false;
          break;
        }
      } else /* Number */ {
        BigDecimal target = data.asObject().get(key).isNumber()
            ? new BigDecimal(data.asObject().get(key).toString())
            : BigDecimal.ZERO;
        RubyArray<Number> numbers =
            Ruby.Array.of(criteria.get(key)).map(o -> (Number) o);

        if (!numbers
            .map(n -> target.compareTo(new BigDecimal(n.toString())) == 0)
            .contains(true)) {
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

  default Iterable<Case> findByUser(Authentication auth) {
    String username = auth.getName();

    if (username.equals("super") || username.equals("admin")) return findAll();

    return findByOwnerEqualsOrManagersInOrEditorsInOrViewersIn(username,
        username, username, username);
  }

  default Iterable<Case> findByUserAndStatus(Authentication auth,
      Case.Status status) {
    Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
    if (authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
        || authorities.contains(new SimpleGrantedAuthority("ROLE_SUPER"))) {
      return findByStatus(status);
    }

    String username = auth.getName();
    QCase qCase = QCase.case$;
    BooleanExpression isStatus = qCase.status.eq(status);
    BooleanExpression isOwner = qCase.owner.eq(username);
    BooleanExpression hasManager = qCase.managers.contains(username);
    BooleanExpression hasEditor = qCase.editors.contains(username);
    BooleanExpression hasViewer = qCase.viewers.contains(username);
    return findAll(
        isStatus.and(isOwner.or(hasManager).or(hasEditor).or(hasViewer)));
  }

  default Iterable<Case> findByOwnerEqualsOrManagersInOrEditorsInOrViewersIn(
      String username1, String username2, String username3, String username4) {
    QCase qCase = QCase.case$;
    BooleanExpression isOwner = qCase.owner.eq(username1);
    BooleanExpression hasManager = qCase.managers.contains(username2);
    BooleanExpression hasEditor = qCase.editors.contains(username3);
    BooleanExpression hasViewer = qCase.viewers.contains(username4);
    return findAll(isOwner.or(hasManager).or(hasEditor).or(hasViewer));
  }

  List<Case> findByStatus(Case.Status status);

  Case findByIrbNumber(String irbNumber);

}
