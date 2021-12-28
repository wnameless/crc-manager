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
package com.wmw.crc.manager.util;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.wnameless.json.base.JacksonJsonValue;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.github.wnameless.json.unflattener.JsonUnflattener;

import lombok.Data;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonDataUriUtils {

  @Data
  public static final class DataURIFormData {

    private final JsonNode formData;
    private final Map<String, String> dataURIs;

    public DataURIFormData(JsonNode formData, Map<String, String> dataURIs) {
      this.formData = formData;
      this.dataURIs = dataURIs;
    }

  }

  public DataURIFormData createDataURIFormData(JsonNode formData)
      throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();

    JacksonJsonValue jsonVal = new JacksonJsonValue(formData);
    JsonNode flattenedMap = mapper.readTree(JsonFlattener.flatten(jsonVal));

    ObjectNode filteredFlattenedMap = mapper.createObjectNode();
    Map<String, String> dataURIs = new LinkedHashMap<>();

    Iterator<String> fnIter = flattenedMap.fieldNames();
    while (fnIter.hasNext()) {
      String field = fnIter.next();
      if (flattenedMap.get(field).isTextual()) {
        if (flattenedMap.get(field).asText().startsWith("data:")) {
          dataURIs.put(field, flattenedMap.get(field).asText());

          filteredFlattenedMap.put(field,
              flattenedMap.get(field).asText().split(",")[0] + ",");
        } else {
          filteredFlattenedMap.put(field, flattenedMap.get(field).asText());
        }
      }
    }

    String filteredJson =
        JsonUnflattener.unflatten(filteredFlattenedMap.toString());
    return new DataURIFormData(new ObjectMapper().readTree(filteredJson),
        dataURIs);
  }

}
