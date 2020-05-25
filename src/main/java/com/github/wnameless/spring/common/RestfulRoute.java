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

public interface RestfulRoute<ID> extends JoinablePath {

  public static <ID> RestfulRoute<ID> of(String indexPath) {
    return new RestfulRoute<ID>() {

      @Override
      public String getIndexPath() {
        return indexPath;
      }

    };
  }

  @Override
  default String getRootPath() {
    return getIndexPath();
  }

  default String idToParam(ID id) {
    return id.toString();
  }

  String getIndexPath();

  default String getCreatePath() {
    return getIndexPath();
  }

  default String getNewPath() {
    return getIndexPath() + "/new";
  }

  default String getEditPath(ID id) {
    return getIndexPath() + "/" + idToParam(id) + "/edit";
  }

  default String getShowPath(ID id) {
    return getIndexPath() + "/" + idToParam(id);
  }

  default String getUpdatePath(ID id) {
    return getIndexPath() + "/" + idToParam(id);
  }

  default String getDestroyPath(ID id) {
    return getIndexPath() + "/" + idToParam(id);
  }

}
