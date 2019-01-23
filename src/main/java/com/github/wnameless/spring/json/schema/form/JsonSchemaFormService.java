/*
 *
 * Copyright 2018 Wei-Ming Wu
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
package com.github.wnameless.spring.json.schema.form;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.github.wnameless.json.JsonPopulatable;

@Service
public class JsonSchemaFormService {

  @Autowired(required = false)
  private JsonDataInterceptor jdi;

  public String toJsonString(JsonSchemaForm jsonSchemaForm) {
    JsonObject jo = Json.object();
    jo.add("jsonData", Json.parse(jsonSchemaForm.getFormData()));
    jo.add("jsonSchema", Json.parse(jsonSchemaForm.getSchema()));
    jo.add("jsonUiSchema", Json.parse(jsonSchemaForm.getUiSchema()));
    return jo.toString();
  }

  public void setJsonData(String jsonData, JsonSchemaForm jsonSchemaForm) {
    if (jdi != null) {
      jdi.accept(jsonData, jsonSchemaForm);
    } else {
      jsonSchemaForm.setFormData(jsonData);
    }

    if (jsonSchemaForm instanceof JsonPopulatable) {
      ((JsonPopulatable) jsonSchemaForm).setPopulatedJson(jsonData);
    }
  }

}
