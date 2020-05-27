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
package com.github.wnameless.spring.common;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;

public final class ModelHelper {

  private ModelHelper() {}

  public static Pageable initPageable(Model model,
      Map<String, String> requestParams, HttpSession session) {
    String page = (String) initAttrWithDefault(model, "page",
        requestParams.get("page"), "0", session);
    String size = (String) initAttrWithDefault(model, "size",
        requestParams.get("size"), "10", session);
    String sort = (String) initAttrWithDefault(model, "sort",
        requestParams.get("sort"), "", session);

    return initPageable(model, PageRequest.of(Integer.valueOf(page),
        Integer.valueOf(size), Pageables.paramToSort(sort)), session);
  }

  public static Pageable initPageableWithDefault(Model model,
      Map<String, String> requestParams, HttpSession session,
      Pageable pageable) {
    String page =
        (String) initAttrWithDefault(model, "page", requestParams.get("page"),
            Integer.toString(pageable.getPageNumber()), session);
    String size =
        (String) initAttrWithDefault(model, "size", requestParams.get("size"),
            Integer.toString(pageable.getPageSize()), session);
    String sort =
        (String) initAttrWithDefault(model, "sort", requestParams.get("sort"),
            Pageables.sortToParam(pageable.getSort()).get(0), session);

    return initPageable(model, PageRequest.of(Integer.valueOf(page),
        Integer.valueOf(size), Pageables.paramToSort(sort)), session);
  }

  public static Pageable initPageable(Model model, Pageable pageable) {
    return initPageable(model, pageable, null);
  }

  public static Pageable initPageable(Model model, Pageable pageable,
      HttpSession session) {
    return (Pageable) initAttrWithDefault(model, "pageable", pageable,
        PageRequest.of(0, 10), session);
  }

  public static Object initAttrWithDefault(Model model, String key,
      Object value, Object defaultVal) {
    return initAttrWithDefault(model, key, value, defaultVal, null);
  }

  public static Object initAttrWithDefault(Model model, String key,
      Object value, Object defaultVal, HttpSession session) {
    if (value == null && session != null) {
      value = session.getAttribute(key);
    }
    if (value == null) value = defaultVal;

    model.addAttribute(key, value);
    if (session != null) session.setAttribute(key, value);

    return value;
  }

  public static Object initAttr(Model model, Map<String, String> requestParams,
      String key, Object value) {
    return initAttr(model, requestParams, key, value, null);
  }

  public static Object initAttr(Model model, Map<String, String> requestParams,
      String key, Object value, HttpSession session) {
    if (!requestParams.containsKey(key)) {
      if (session != null && session.getAttribute(key) != null) {
        value = session.getAttribute(key);
      }
    }

    model.addAttribute(key, value);
    if (session != null) session.setAttribute(key, value);

    return value;
  }

}
