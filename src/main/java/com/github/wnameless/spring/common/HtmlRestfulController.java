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

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface HtmlRestfulController<ID, E> {

  @GetMapping
  String indexHtml(Model model);

  @PostMapping
  String createHtml(Model model, @RequestBody E data);

  @GetMapping("/new")
  String newHtml(Model model);

  @GetMapping("/{id}/edit")
  String editHtml(Model model, @PathVariable(name = "id") ID id);

  @GetMapping("/{id}")
  String showHtml(Model model, @PathVariable(name = "id") ID id);

  @PutMapping("/{id}")
  @PostMapping("/{id}")
  String updateHtml(Model model, @PathVariable(name = "id") ID id,
      @RequestBody E data);

  @DeleteMapping("/{id}")
  String destroyHtml(Model model, @PathVariable(name = "id") ID id);

}
