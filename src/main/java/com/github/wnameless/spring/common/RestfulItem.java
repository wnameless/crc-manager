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
package com.github.wnameless.spring.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface RestfulItem<ID> {

  String getResourcePath();

  ID getId();

  default String getIndexPath() {
    return getResourcePath();
  }

  default String getCreatePath() {
    return getIndexPath();
  }

  default String getNewPath() {
    return getIndexPath() + "/new";
  }

  default String getEditPath() {
    return getIndexPath() + "/" + getId() + "/edit";
  }

  default String getShowPath() {
    return getIndexPath() + "/" + getId();
  }

  default String getUpdatePath() {
    return getIndexPath() + "/" + getId();
  }

  default String getDestroyPath() {
    return getIndexPath() + "/" + getId();
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

  default RestfulItem<ID> withParent(RestfulItem<?> parent) {
    String resourcePath = parent.getShowPath() + getResourcePath();
    ID id = getId();

    return new RestfulItem<ID>() {

      @Override
      public String getResourcePath() {
        return resourcePath;
      }

      @Override
      public ID getId() {
        return id;
      }

    };
  }

  default <CID> RestfulItem<CID> withChild(RestfulItem<CID> child) {
    String resourcePath = getShowPath() + child.getResourcePath();
    CID id = child.getId();

    return new RestfulItem<CID>() {

      @Override
      public String getResourcePath() {
        return resourcePath;
      }

      @Override
      public CID getId() {
        return id;
      }

    };
  }

}
