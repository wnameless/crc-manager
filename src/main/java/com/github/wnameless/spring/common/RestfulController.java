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

import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

public interface RestfulController<I, ID, R extends CrudRepository<I, ID>, T extends Enum<? extends RestfulResource>> {

  T getRestfulResource();

  R getRepository();

  @ModelAttribute("resourceName")
  default String getResourceName() {
    return ((RestfulResource) getRestfulResource()).getResourceName();
  }

  @ModelAttribute("resourceItem")
  default I getResourceItem(@PathVariable(required = false) ID id) {
    if (id != null) {
      return getRepository().findById(id).get();
    }
    return null;
  }

}
