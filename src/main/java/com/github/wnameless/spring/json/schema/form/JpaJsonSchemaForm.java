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
package com.github.wnameless.spring.json.schema.form;

import static com.google.common.collect.Maps.newLinkedHashMap;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@MappedSuperclass
public abstract class JpaJsonSchemaForm implements JsonSchemaForm {

  @Column
  @Lob
  protected String jsonData = "{}";

  @Column
  @Lob
  protected String jsonSchema = "{}";

  @Column
  @Lob
  protected String jsonUiSchema = "{}";

  public static final String UTF8_BOM = "\uFEFF";

  private static String removeUTF8BOM(String s) {
    if (s.startsWith(UTF8_BOM)) {
      s = s.substring(1);
    }
    return s;
  }

  @Override
  public String getJsonData() {
    return jsonData;
  }

  @Override
  public void setJsonData(String jsonData) {

    this.jsonData = removeUTF8BOM(jsonData);
  }

  @Override
  public String getJsonSchema() {
    return jsonSchema;
  }

  @Override
  public void setJsonSchema(String jsonSchema) {
    this.jsonSchema = removeUTF8BOM(jsonSchema);
  }

  @Override
  public String getJsonUiSchema() {
    return jsonUiSchema;
  }

  @Override
  public void setJsonUiSchema(String jsonUiSchema) {
    this.jsonUiSchema = removeUTF8BOM(jsonUiSchema);
  }

  public Map<String, String> propertyTitles() {
    Gson gson = new Gson();

    Map<String, String> propertyTitles = newLinkedHashMap();

    Map<String, Object> jsonSchema = gson.fromJson(getJsonSchema(),
        new TypeToken<Map<String, Object>>() {}.getType());

    @SuppressWarnings("unchecked")
    Map<String, Map<String, Object>> jsonSchemaProperties =
        (Map<String, Map<String, Object>>) jsonSchema.get("properties");

    for (String key : jsonSchemaProperties.keySet()) {
      propertyTitles.put(key,
          (String) jsonSchemaProperties.get(key).get("title"));
    }

    return propertyTitles;
  }

}
