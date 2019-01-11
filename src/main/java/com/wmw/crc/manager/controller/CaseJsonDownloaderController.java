/*
 *
 * Copyright 2018 Wei-Ming Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.wmw.crc.manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.github.wnameless.spring.json.schema.form.JsonSchemaForm;
import com.wmw.crc.manager.repository.CaseRepository;

@Profile("test")
@RestController
public class CaseJsonDownloaderController {

  @Autowired
  CaseRepository caseRepository;

  @GetMapping("/json/cases/{id}")
  JsonSchemaForm caseJsonScheme(@PathVariable("id") Long id) {
    return caseRepository.findById(id).get();
  }

}