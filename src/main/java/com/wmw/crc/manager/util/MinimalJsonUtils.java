package com.wmw.crc.manager.util;

import com.eclipsesource.json.JsonValue;

public class MinimalJsonUtils {

  private MinimalJsonUtils() {}

  public static String findFirstAsString(JsonValue jv, String key) {
    if (jv.isObject()) {
      if (jv.asObject().names().contains(key)) {
        if (jv.asObject().get("name").isString()) {
          return jv.asObject().get("name").asString();
        } else {
          return jv.asObject().get("name").toString();
        }
      }
    } else if (jv.isArray()) {
      for (JsonValue v : jv.asArray()) {
        if (v.isObject()) {
          if (v.asObject().names().contains(key)) {
            if (v.asObject().get("name").isString()) {
              return v.asObject().get("name").asString();
            } else {
              return v.asObject().get("name").toString();
            }
          }
        }
      }
    }
    return "";
  }

}
