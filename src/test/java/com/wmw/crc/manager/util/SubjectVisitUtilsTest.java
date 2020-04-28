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
package com.wmw.crc.manager.util;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.wmw.crc.manager.model.Visit;

public class SubjectVisitUtilsTest {

  @Test
  public void testTrimVisits() {
    List<Visit> visits = new ArrayList<>();

    Visit visit = new Visit();
    visit.setDate(LocalDate.now());
    visit.setDivision("A");
    visit.setDoctor("a");
    visits.add(visit);

    visit = new Visit();
    visit.setDate(LocalDate.now());
    visit.setDivision("B");
    visit.setDoctor("b");
    visits.add(visit);

    assertEquals(1, SubjectVisitUtils.trimVisits(visits).size());
  }

}
