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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.wmw.crc.manager.model.Visit;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SubjectVisitUtils {

  public Collection<Visit> trimVisits(List<Visit> visits) {
    ListMultimap<LocalDate, Visit> trimmedDatedVisits =
        ArrayListMultimap.create();

    ListMultimap<LocalDate, Visit> datedVisits = ArrayListMultimap.create();
    visits.forEach(v -> {
      datedVisits.put(v.getDate(), v);
    });

    for (LocalDate key : datedVisits.keySet()) {
      List<Visit> trimmedVisits = new ArrayList<>();

      boolean hasContraindication = false;
      for (Visit v : datedVisits.get(key)) {
        if (v.isContraindicationSuspected()) hasContraindication = true;

        if (hasContraindication) {
          trimmedVisits.add(v);
        } else {
          trimmedVisits.clear();
          trimmedVisits.add(v);
        }
      }

      // datedVisits.putAll(key, trimmedVisits);
      trimmedDatedVisits.putAll(key, trimmedVisits);
    }

    return trimmedDatedVisits.values();
  }

}
