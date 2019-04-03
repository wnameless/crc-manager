/*
 *
 * Copyright 2019 Wei-Ming Wu
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
package com.wmw.crc.manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wmw.crc.manager.service.TsghService;

@Profile("test")
@Controller
public class TestController {

  @Autowired
  TsghService tsghService;

  @PreAuthorize("@perm.isAdmin()")
  @GetMapping("test/medicines/refresh")
  @ResponseBody
  String refreshMedicines() {
    return tsghService.refreshMedicines().getInfomation();
  }

  @PreAuthorize("@perm.isAdmin()")
  @GetMapping("test/contraindications/refresh")
  @ResponseBody
  String refreshContraindications() {
    return tsghService.refreshContraindications().getInfomation();
  }

}
