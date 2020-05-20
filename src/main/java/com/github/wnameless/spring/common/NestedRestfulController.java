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

import java.util.function.BiPredicate;

import org.springframework.data.repository.CrudRepository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

public interface NestedRestfulController< //
    P extends RestfulItem<PID>, PID, PR extends CrudRepository<P, PID>, PRR extends RestfulResource, //
    C extends RestfulItem<CID>, CID, CR extends CrudRepository<C, CID>, CRR extends RestfulResource> {

  PRR getParentRestfulResource();

  PR getParentRepository();

  CRR getRestfulResource();

  CR getRepository();

  BiPredicate<P, C> getPaternityTesting();

  Iterable<C> getResourceItems(P parent);

  default String getParentResourcePathKey() {
    return "parentResourcePath";
  }

  @ModelAttribute
  default void setParentResourcePath(Model model) {
    model.addAttribute(getParentResourcePathKey(),
        "/" + getParentRestfulResource().getResourceName());
  }

  default String getParentResourceItemKey() {
    return "parentResourceItem";
  }

  @ModelAttribute
  default void setParentResourceItem(Model model,
      @PathVariable(required = false) PID parentId) {
    if (parentId != null) {
      P parent = getParentRepository().findById(parentId).get();
      model.addAttribute(getParentResourceItemKey(), parent);
    }
  }

  default P getParentResourceItem(PID parentId) {
    return getParentResourceItem(parentId, null);
  }

  default P getParentResourceItem(PID parentId, P defaultItem) {
    if (parentId != null) {
      return getParentRepository().findById(parentId).get();
    }
    return defaultItem;
  }

  default String getResourcePathKey() {
    return "resourcePath";
  }

  @ModelAttribute
  default void setResourcePath(Model model) {
    model.addAttribute(getResourcePathKey(),
        "/" + getRestfulResource().getResourceName());
  }

  default String getResourceItemKey() {
    return "resourceItem";
  }

  @ModelAttribute
  default void setResourceItem(Model model,
      @PathVariable(required = false) PID parentId,
      @PathVariable(required = false) CID id) {
    if (parentId != null && id != null) {
      P parent = getParentRepository().findById(parentId).get();
      C child = getRepository().findById(id).get();

      if (getPaternityTesting().test(parent, child)) {
        model.addAttribute(getResourceItemKey(), child);
      }
    }
  }

  default C getResourceItem(PID parentId, CID id) {
    return getResourceItem(parentId, id, null);
  }

  default C getResourceItem(PID parentId, CID id, C defaultItem) {
    if (parentId != null && id != null) {
      P parent = getParentRepository().findById(parentId).get();
      C child = getRepository().findById(id).get();
      if (getPaternityTesting().test(parent, child)) {
        return child;
      }
    }

    return defaultItem;
  }

  default String getResourceItemsKey() {
    return "resourceItems";
  }

  @ModelAttribute
  default void setResourceItems(Model model,
      @PathVariable(required = false) PID parentId) {
    if (parentId != null) {
      P parent = getParentRepository().findById(parentId).get();
      Iterable<C> childs = getResourceItems(parent);
      model.addAttribute(getResourceItemsKey(), childs);
    }
  }

}
