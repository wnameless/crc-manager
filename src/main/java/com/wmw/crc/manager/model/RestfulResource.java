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
package com.wmw.crc.manager.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface RestfulResource<ID> {

  String getResourceName();

  ID getId();

  default String getIndexPath() {
    return "/" + getResourceName();
  }

  default String getCreatePath() {
    return "/" + getResourceName();
  }

  default String getNewPath() {
    return "/" + getResourceName() + "/new";
  }

  default String getEditPath() {
    return "/" + getResourceName() + "/" + getId() + "/edit";
  }

  default String getShowPath() {
    return "/" + getResourceName() + "/" + getId();
  }

  default String getUpdatePath() {
    return "/" + getResourceName() + "/" + getId();
  }

  default String getDestroyPath() {
    return "/" + getResourceName() + "/" + getId();
  }

  default String joinPath(String... paths) {
    String pathSeprator = "/";

    List<String> list = new ArrayList<>(Arrays.asList(paths));
    list.add(0, getShowPath());
    for (int i = 1; i < list.size(); i++) {
      int predecessor = i - 1;
      while (list.get(predecessor).endsWith(pathSeprator)) {
        list.set(predecessor, list.get(predecessor).substring(0,
            list.get(predecessor).length() - 1));
      }
      while (list.get(i).startsWith(pathSeprator)) {
        list.set(i, list.get(i).substring(1));
      }
      list.set(i, pathSeprator + list.get(i));
    }

    StringBuilder sb = new StringBuilder();
    list.stream().forEach(path -> {
      sb.append(path);
    });
    return sb.toString();
  }

}
