/*
 *
 * Copyright 2017 Wei-Ming Wu
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

import static com.google.common.base.Charsets.UTF_8;

import java.io.IOException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.io.Resources;
import com.wmw.crc.manager.form.json.schema.FormJsonSchemaProvider;

@Controller
public class IndexController {

  @Autowired
  FormJsonSchemaProvider schemaProvider;

  @RequestMapping(path = { "/", "index" })
  String index(@RequestParam(value = "name", required = false,
      defaultValue = "Sam") String name, Model model) throws IOException {
    model.addAttribute("name", name);

    model.addAttribute("formSchema", schemaProvider.getFormSchema());
    model.addAttribute("formUiSchema", schemaProvider.getFormUiSchema());
    model.addAttribute("formSupplement1Schema",
        schemaProvider.getFormSupplement1Schema());
    model.addAttribute("formSupplement1UiSchema",
        schemaProvider.getFormSupplement1UiSchema());
    model.addAttribute("formSupplement2Schema",
        schemaProvider.getFormSupplement2Schema());
    model.addAttribute("formSupplement2UiSchema",
        schemaProvider.getFormSupplement2UiSchema());

    URL url = Resources.getResource("json-schema/form-data.json");
    String formData = Resources.toString(url, UTF_8);
    model.addAttribute("formData", formData);

    return "index";
  }

}