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

import javax.persistence.Column;
import javax.persistence.Convert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.jpa.type.flattenedjson.FlattenedJsonTypeConfigurer;
import com.github.wnameless.jpa.type.flattenedjson.JsonNodeConverter;
import com.github.wnameless.json.beanpopulator.JsonPopulatable;
import com.github.wnameless.spring.react.jsf.ReactJsonSchemaForm;
import com.wmw.crc.manager.JsonSchema;

import lombok.Data;

@Data
public class BundleDescription implements JsonPopulatable, ReactJsonSchemaForm {

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
    return JsonSchema.BUNDLE_DESCRIPTION_SCHEMA;
  }

  @Override
  public JsonNode getUiSchema() {
    return JsonSchema.BUNDLE_DESCRIPTION_UI_SCHEMA;
  }

}