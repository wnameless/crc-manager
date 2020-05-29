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

import org.springframework.data.repository.CrudRepository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

public interface RestfulController< //
    I extends RestfulItem<ID>, ID, R extends CrudRepository<I, ID>> {

  RestfulRoute<ID> getRoute();

  R getRepository();

  void configure(ModelPolicy<I> policy);

  default ModelPolicy<I> getOption() {
    ModelPolicy<I> policy = new ModelPolicy<I>();
    configure(policy);
    return policy;
  }

  @ModelAttribute
  default void setItem(Model model, @PathVariable(required = false) ID id) {
    if (!getOption().isEnable()) return;

    I item = null;

    if (id != null) {
      item = getRepository().findById(id).get();
    }

    if (getOption().getAfterInit() != null) {
      item = getOption().getAfterInit().apply(item);
    }

    model.addAttribute(getItemKey(), getOption().getBeforeAdd() == null ? item
        : getOption().getBeforeAdd().apply(item));
  }

  @ModelAttribute
  default void setRoute(Model model) {
    model.addAttribute(getRouteKey(), getRoute());
  }

  default String getRouteKey() {
    return "route";
  }

  default String getItemKey() {
    return "item";
  }

  default I getItem(ID id) {
    return getItem(id, null);
  }

  default I getItem(ID id, I defaultItem) {
    if (id != null) {
      return getRepository().findById(id).get();
    }
    return defaultItem;
  }

  default I updateItem(Model model, I item) {
    model.addAttribute(getItemKey(), item);
    return null;
  }

}
