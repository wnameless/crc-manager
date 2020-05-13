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
package com.wmw.crc.manager.util;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonNodeUtils {

  public String findFirstAsString(JsonNode jn, String key) {
    if (jn == null) return "";

    if (jn.isObject()) {
      if (jn.has(key)) {
        if (jn.get("name").isTextual()) {
          return jn.get("name").textValue();
        } else {
          return jn.get("name").asText();
        }
      }
    } else if (jn.isArray()) {
      Iterator<JsonNode> elements = jn.iterator();
      while (elements.hasNext()) {
        JsonNode v = elements.next();
        if (v.isObject()) {
          if (v.has(key)) {
            if (v.get("name").isTextual()) {
              return v.get("name").textValue();
            } else {
              return v.get("name").asText();
            }
          }
        }
      }
    }
    return "";
  }

  public List<String> splitValToList(JsonNode jv) {
    List<String> vals = newArrayList();

    if (jv.isArray()) {
      Iterator<JsonNode> elements = jv.iterator();
      while (elements.hasNext()) {
        JsonNode val = elements.next();
        if (val.isObject() && val.has("name")) {
          vals.add(val2String(val.get("name")));
        } else {
          vals.add(val2String(val));
        }
      }
    } else if (jv.isObject() && jv.has("name")) {
      vals.add(val2String(jv.get("name")));
    } else {
      vals.add(val2String(jv));
    }

    return vals;
  }

  public String val2String(JsonNode val) {
    return val.isTextual() ? val.textValue() : val.asText();
  }

}
