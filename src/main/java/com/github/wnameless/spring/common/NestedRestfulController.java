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

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.data.repository.CrudRepository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

public interface NestedRestfulController< //
    P extends RestfulItem<PID>, PID, PR extends CrudRepository<P, PID>, //
    C extends RestfulItem<CID>, CID, CR extends CrudRepository<C, CID>> {

  Function<P, RestfulRoute<CID>> getRoute();

  PR getParentRepository();

  CR getRepository();

  BiPredicate<P, C> getPaternityTesting();

  Iterable<C> getChildren(P parent);

  BiConsumer<P, C> afterInitParentAndChild();

  @ModelAttribute
  default void setParentAndChild(Model model,
      @PathVariable(required = false) PID parentId,
      @PathVariable(required = false) CID id) {
    P parent = null;
    C child = null;

    if (parentId != null) {
      parent = getParentRepository().findById(parentId).get();
      model.addAttribute(getParentKey(), parent);

      if (id != null) {
        child = getRepository().findById(id).get();

        if (getPaternityTesting().test(parent, child)) {
          model.addAttribute(getChildKey(), child);
        } else {
          child = null;
        }
      }
    }

    if (afterInitParentAndChild() != null) {
      afterInitParentAndChild().accept(parent, child);
    }
  }

  @ModelAttribute
  default void setChildren(Model model,
      @PathVariable(required = false) PID parentId,
      @PathVariable(required = false) CID id) {
    Iterable<C> children = null;

    if (parentId != null && id == null) {
      P parent = getParentRepository().findById(parentId).get();
      children = getChildren(parent);
      model.addAttribute(getChildrenKey(), children);
    }

    if (afterInitChildren() != null) {
      afterInitChildren().accept(children);
    }
  }

  default Consumer<? super Iterable<C>> afterInitChildren() {
    return null;
  }

  @ModelAttribute
  default void setRoute(Model model,
      @PathVariable(required = false) PID parentId) {
    if (parentId != null) {
      model.addAttribute(getRouteKey(), getRoute().apply(getParent(parentId)));
    }
  }

  default String getRouteKey() {
    return "route";
  }

  default String getParentKey() {
    return "parent";
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

  default P updateParent(Model model, P parent) {
    model.addAttribute(getParentKey(), parent);
    return parent;
  }

  default String getChildKey() {
    return "child";
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

  default C updateChild(Model model, C child) {
    model.addAttribute(getChildKey(), child);
    return child;
  }

  default String getChildrenKey() {
    return "children";
  }

  default Iterable<C> updateChildren(Model model, Iterable<C> children) {
    model.addAttribute(getChildrenKey(), children);
    return children;
  }

  default Iterable<C> updateChildrenByParent(Model model, P parent) {
    Iterable<C> children = getChildren(parent);
    model.addAttribute(getChildrenKey(), children);
    return children;
  }

}
