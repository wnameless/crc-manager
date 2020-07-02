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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.experimental.UtilityClass;
import net.sf.rubycollect4j.Ruby;

@UtilityClass
public class JsonNodeUtils {

  public List<String> getStringifyValues(JsonNode jn, String key) {
    if (jn == null) return Collections.emptyList();

    if (jn.isObject()) {
      ObjectNode on = (ObjectNode) jn;
      return Ruby.Array.copyOf(on.fields()).map(f -> f.getValue())
          .map(i -> i.asText()).toList();
    } else if (jn.isArray()) {
      ArrayNode an = (ArrayNode) jn;
      return Ruby.Array.copyOf(an.iterator()).map(i -> i.asText()).toList();
    }

    return Collections.emptyList();
  }

  public String findFirstAsString(JsonNode jn, String key) {
    if (jn == null) return "";

    if (jn.isObject()) {
      if (jn.has(key)) {
        if (jn.get(key).isTextual()) {
          return jn.get(key).textValue();
        } else {
          return jn.get(key).asText();
        }
      }
    } else if (jn.isArray()) {
      Iterator<JsonNode> elements = jn.iterator();
      while (elements.hasNext()) {
        JsonNode v = elements.next();
        if (v.isObject()) {
          if (v.has(key)) {
            if (v.get(key).isTextual()) {
              return v.get(key).textValue();
            } else {
              return v.get(key).asText();
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
