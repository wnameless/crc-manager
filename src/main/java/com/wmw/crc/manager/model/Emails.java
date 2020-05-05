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
package com.wmw.crc.manager.model;

import static com.google.common.base.Charsets.UTF_8;

import java.io.IOException;
import java.net.URL;

import javax.persistence.Column;
import javax.persistence.Convert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.jpa.type.flattenedjson.FlattenedJsonTypeConfigurer;
import com.github.wnameless.jpa.type.flattenedjson.JsonNodeConverter;
import com.github.wnameless.json.JsonPopulatable;
import com.github.wnameless.spring.react.ReactJsonSchemaForm;
import com.google.common.io.Resources;
import com.wmw.crc.manager.JsonSchemaPath;

import lombok.Data;

@Data
public class Emails implements JsonPopulatable, ReactJsonSchemaForm {

  public static final JsonNode SCHEMA;
  public static final JsonNode UI_SCHEMA;
  static {
    URL url = Resources.getResource(JsonSchemaPath.emailsSchema);
    JsonNode jsonNode = null;
    try {
      jsonNode = new ObjectMapper().readTree(Resources.toString(url, UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
    SCHEMA = jsonNode;
    try {
      jsonNode = new ObjectMapper().readTree("{}");
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    UI_SCHEMA = jsonNode;
  }

  @Convert(converter = JsonNodeConverter.class)
  @Column(columnDefinition = "text")
  protected JsonNode formData = FlattenedJsonTypeConfigurer.INSTANCE
      .getObjectMapperFactory().get().createObjectNode();

  @Override
  public void setFormData(JsonNode formData) {
    this.formData = formData;
    try {
      setPopulatedJson(new ObjectMapper().writeValueAsString(formData));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public JsonNode getSchema() {
    return SCHEMA;
  }

  @Override
  public void setSchema(JsonNode schema) {}

  @Override
  public JsonNode getUiSchema() {
    return UI_SCHEMA;
  }

  @Override
  public void setUiSchema(JsonNode uiSchema) {}

}
