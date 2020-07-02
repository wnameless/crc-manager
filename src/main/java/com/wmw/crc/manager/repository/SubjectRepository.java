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
import java.util.List;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.util.Criterion;
import com.wmw.crc.manager.util.JsonNodeUtils;

import net.sf.rubycollect4j.Ruby;
import net.sf.rubycollect4j.RubyArray;

@JaversSpringDataAuditable
@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

  default List<Subject> findAllByCaseStudyInAndCriteria(
      Iterable<CaseStudy> cases, List<Criterion> criteria) {
    List<Subject> subjects = findAllByCaseStudyIn(cases);
    ListMultimap<String, Object> groupedCriteria = groupedCriteria(criteria);

    return Ruby.Array.of(subjects)
        .keepIf(s -> isCriteriaMatch(s.getFormData(), groupedCriteria))
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

  List<Subject> findAllByCaseStudyIn(Iterable<CaseStudy> cases);

  List<Subject> findAllByCaseStudy(CaseStudy caseStudy);

  boolean existsByCaseStudyAndNationalIdAndDropoutDate(CaseStudy caseStudy,
      String nationalId, String dropoutDate);

  Subject findByIdAndCaseStudy(Long id, CaseStudy caseStudy);

  boolean existsByIdAndCaseStudy(Long id, CaseStudy caseStudy);

}
