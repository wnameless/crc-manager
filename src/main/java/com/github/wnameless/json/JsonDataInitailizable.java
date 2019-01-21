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
package com.github.wnameless.json;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.github.wnameless.json.flattener.JsonFlattener;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

public interface JsonDataInitailizable {

  default void setJsonInitData(String jsonData) {
    jsonData = JsonFlattener.flatten(jsonData);
    JsonValue jv = Json.parse(jsonData);
    JsonObject jo = jv.asObject();
    List<String> keys = jo.names();

    Class<?> klass = this.getClass();
    for (Field f : klass.getDeclaredFields()) {
      if (f.isAnnotationPresent(JsonInitKey.class)) {
        JsonInitKey anno = f.getAnnotation(JsonInitKey.class);
        String varName = anno.value();
        if (keys.contains(varName)) {
          JsonValue val = jo.get(varName);
          Type type = f.getGenericType();
          if (val.isString() && type.getClass().isAssignableFrom(String.class.getClass())) {
            f.setAccessible(true);
            try {
              f.set(this, val.asString());
            } catch (IllegalArgumentException | IllegalAccessException e) {
            }
          } else if (val.isNumber() && type.getClass().isAssignableFrom(Integer.class.getClass())) {
            f.setAccessible(true);
            try {
              f.set(this, val.asInt());
            } catch (IllegalArgumentException | IllegalAccessException e) {
            }
          } else if (val.isNumber() && type.getClass().isAssignableFrom(Long.class.getClass())) {
            f.setAccessible(true);
            try {
              f.set(this, val.asLong());
            } catch (IllegalArgumentException | IllegalAccessException e) {
            }
          } else if (val.isNumber() && type.getClass().isAssignableFrom(Double.class.getClass())) {
            f.setAccessible(true);
            try {
              f.set(this, val.asDouble());
            } catch (IllegalArgumentException | IllegalAccessException e) {
            }
          } else if (val.isNumber() && type.getClass().isAssignableFrom(Float.class.getClass())) {
            f.setAccessible(true);
            try {
              f.set(this, val.asFloat());
            } catch (IllegalArgumentException | IllegalAccessException e) {
            }
          } else if (val.isBoolean()
              && type.getClass().isAssignableFrom(Boolean.class.getClass())) {
            f.setAccessible(true);
            try {
              f.set(this, val.asBoolean());
            } catch (IllegalArgumentException | IllegalAccessException e) {
            }
          }
        }
      } else if (f.isAnnotationPresent(JsonInitValue.class)) {
        JsonInitValue jiv = f.getAnnotation(JsonInitValue.class);
        JsonInitValueCustomizer jivc;
        try {
          jivc = jiv.value().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
          throw new RuntimeException(e);
        }
        try {
          f.setAccessible(true);
          f.set(this, jivc.toValue(jsonData));
        } catch (IllegalArgumentException | IllegalAccessException e) {
        }
      }
    }
  }
}
