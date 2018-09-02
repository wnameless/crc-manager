/*
 *
 * Copyright 2018 Wei-Ming Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.wmw.crc.manager.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EntityUtils {

  private EntityUtils() {}

  public static <T> T findChildById(List<T> coll, Long id,
      Function<T, Long> getId) {
    coll = coll.stream().filter(t -> id.equals(getId.apply(t)))
        .collect(Collectors.toList());
    return coll.isEmpty() ? null : coll.get(0);
  }

  public static <T, V> T findChildByValue(List<T> coll, V value,
      Function<T, V> getValue) {
    coll = coll.stream().filter(t -> value.equals(getValue.apply(t)))
        .collect(Collectors.toList());
    return coll.isEmpty() ? null : coll.get(0);
  }

}
