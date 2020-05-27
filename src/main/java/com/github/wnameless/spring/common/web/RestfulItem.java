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
package com.github.wnameless.spring.common.web;

public interface RestfulItem<ID> extends JoinablePath {

  @Override
  default String getRootPath() {
    return getShowPath();
  }

  ID getId();

  String getIndexPath();

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

  default RestfulItem<ID> withParent(RestfulItem<?> parent) {
    String indexPath = parent.getShowPath() + getIndexPath();
    ID id = getId();

    return new RestfulItem<ID>() {

      @Override
      public String getIndexPath() {
        return indexPath;
      }

      @Override
      public ID getId() {
        return id;
      }

    };
  }

  default <CID> RestfulItem<CID> withChild(RestfulItem<CID> child) {
    String indexPath = getShowPath() + child.getIndexPath();
    CID id = child.getId();

    return new RestfulItem<CID>() {

      @Override
      public String getIndexPath() {
        return indexPath;
      }

      @Override
      public CID getId() {
        return id;
      }

    };
  }

}
