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
import java.util.function.Function;

import org.springframework.data.repository.CrudRepository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

public interface NestedRestfulController< //
    P extends RestfulItem<PID>, PID, PR extends CrudRepository<P, PID>, PRR extends RestfulRoute<PID>, //
    C extends RestfulItem<CID>, CID, CR extends CrudRepository<C, CID>, CRR extends RestfulRoute<CID>> {

  PR getParentRepository();

  CR getRepository();

  BiPredicate<P, C> getPaternityTesting();

  Function<P, RestfulRoute<CID>> getRoute();

  default String getRouteKey() {
    return "route";
  }

  @ModelAttribute
  default void setRoute(Model model,
      @PathVariable(required = false) PID parentId) {
    if (parentId != null) {
      model.addAttribute(getRouteKey(),
          getRoute().apply(getParentRepository().findById(parentId).get()));
    }
  }

  default String getParentKey() {
    return "parent";
  }

  @ModelAttribute
  default void setParent(Model model,
      @PathVariable(required = false) PID parentId) {
    if (parentId != null) {
      P parent = getParentRepository().findById(parentId).get();
      model.addAttribute(getParentKey(), parent);
    }
  }

  default P getParent(PID parentId) {
    return getParent(parentId, null);
  }

  default P getParent(PID parentId, P defaultItem) {
    if (parentId != null) {
      return getParentRepository().findById(parentId).get();
    }
    return defaultItem;
  }

  default String getChildKey() {
    return "child";
  }

  @ModelAttribute
  default void setChild(Model model,
      @PathVariable(required = false) PID parentId,
      @PathVariable(required = false) CID id) {
    if (parentId != null && id != null) {
      P parent = getParentRepository().findById(parentId).get();
      C child = getRepository().findById(id).get();

      if (getPaternityTesting().test(parent, child)) {
        model.addAttribute(getChildKey(), child);
      }
    }
  }

  default C getChild(PID parentId, CID id) {
    return getChild(parentId, id, null);
  }

  default C getChild(PID parentId, CID id, C defaultItem) {
    if (parentId != null && id != null) {
      P parent = getParentRepository().findById(parentId).get();
      C child = getRepository().findById(id).get();
      if (getPaternityTesting().test(parent, child)) {
        return child;
      }
    }

    return defaultItem;
  }

  default String getChildrenKey() {
    return "children";
  }

  @ModelAttribute
  default void setChildren(Model model,
      @PathVariable(required = false) PID parentId) {
    if (parentId != null) {
      P parent = getParentRepository().findById(parentId).get();
      Iterable<C> childs = getChildren(parent);
      model.addAttribute(getChildrenKey(), childs);
    }
  }

  Iterable<C> getChildren(P parent);

}
