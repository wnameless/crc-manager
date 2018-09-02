package com.wmw.crc.manager.util;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

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

  public static List<String> splitValToList(JsonValue jv) {
    List<String> vals = newArrayList();

    if (jv.isArray()) {
      for (JsonValue val : jv.asArray()) {
        if (val.isObject() && val.asObject().names().contains("name")) {
          vals.add(val2String(val.asObject().get("name")));
        } else {
          vals.add(val2String(val));
        }
      }
    } else if (jv.isObject() && jv.asObject().names().contains("name")) {
      vals.add(val2String(jv.asObject().get("name")));
    } else {
      vals.add(val2String(jv));
    }

    return vals;
  }

  public static String val2String(JsonValue val) {
    return val.isString() ? val.asString() : val.toString();
  }

}
