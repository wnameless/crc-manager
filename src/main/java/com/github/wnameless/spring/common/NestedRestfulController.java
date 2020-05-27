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
    P extends RestfulItem<PID>, PID, PR extends CrudRepository<P, PID>, //
    C extends RestfulItem<CID>, CID, CR extends CrudRepository<C, CID>> {

  Function<P, RestfulRoute<CID>> getRoute();

  PR getParentRepository();

  CR getChildRepository();

  BiPredicate<P, C> getPaternityTesting();

  Iterable<C> getChildren(P parent);

  void configure(ModelOption<P> parentOption, ModelOption<C> childOption,
      ModelOption<? extends Iterable<C>> childrenOption);

  default ModelOption<P> getParentModelOption() {
    ModelOption<P> parentOption = new ModelOption<>();
    ModelOption<C> childOption = new ModelOption<>();
    ModelOption<? extends Iterable<C>> childrenOption = new ModelOption<>();
    configure(parentOption, childOption, childrenOption);
    return parentOption;
  }

  default ModelOption<C> getChildModelOption() {
    ModelOption<P> parentOption = new ModelOption<>();
    ModelOption<C> childOption = new ModelOption<>();
    ModelOption<? extends Iterable<C>> childrenOption = new ModelOption<>();
    configure(parentOption, childOption, childrenOption);
    return childOption;
  }

  default ModelOption<Iterable<C>> getChildrenModelOption() {
    ModelOption<P> parentOption = new ModelOption<>();
    ModelOption<C> childOption = new ModelOption<>();
    ModelOption<Iterable<C>> childrenOption = new ModelOption<>();
    configure(parentOption, childOption, childrenOption);
    return childrenOption;
  }

  @ModelAttribute
  default void setParentAndChild(Model model,
      @PathVariable(required = false) PID parentId,
      @PathVariable(required = false) CID id) {
    if (!getParentModelOption().isInit()) return;

    P parent = null;
    if (parentId != null) {
      parent = getParentRepository().findById(parentId).get();
    }
    if (getParentModelOption().getAfterInitAction() != null) {
      parent = getParentModelOption().getAfterInitAction().apply(parent);
    }
    model.addAttribute(getParentKey(),
        getParentModelOption().getBeforeSetAction() == null ? parent
            : getParentModelOption().getBeforeSetAction().apply(parent));

    if (!getChildModelOption().isInit()) return;

    C child = null;
    if (parent != null && id != null) {
      child = getChildRepository().findById(id).get();
      child = getPaternityTesting().test(parent, child) ? child : null;
    }
    if (getChildModelOption().getAfterInitAction() != null) {
      child = getChildModelOption().getAfterInitAction().apply(child);
    }
    model.addAttribute(getChildKey(),
        getChildModelOption().getBeforeSetAction() == null ? child
            : getChildModelOption().getBeforeSetAction().apply(child));
  }

  @ModelAttribute
  default void setChildren(Model model,
      @PathVariable(required = false) PID parentId,
      @PathVariable(required = false) CID id) {
    if (!getChildrenModelOption().isInit()) return;

    Iterable<C> children = null;

    if (parentId != null && id == null) {
      P parent = getParentRepository().findById(parentId).get();
      children = getChildren(parent);
    }

    if (getChildrenModelOption().getAfterInitAction() != null) {
      children = getChildrenModelOption().getAfterInitAction().apply(children);
    }

    model.addAttribute(getChildrenKey(),
        getChildrenModelOption().getBeforeSetAction() == null ? children
            : getChildrenModelOption().getBeforeSetAction().apply(children));
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
      C child = getChildRepository().findById(id).get();
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
