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
package com.github.wnameless.spring.common.web;

public final class ModelHelper {

  private ModelHelper() {}

  // public static Pageable initPageable(Model model,
  // Map<String, String> requestParams, HttpSession session) {
  // String page = initAttrWithDefault(model, "page", requestParams.get("page"),
  // "0", session);
  // String size = initAttrWithDefault(model, "size", requestParams.get("size"),
  // "10", session);
  // String sort = initAttrWithDefault(model, "sort", requestParams.get("sort"),
  // "", session);
  //
  // return initPageable(model, PageRequest.of(Integer.valueOf(page),
  // Integer.valueOf(size), Pageables.paramToSort(sort)), session);
  // }
  //
  // public static Pageable initPageableWithDefault(Model model,
  // Map<String, String> requestParams, Pageable pageable) {
  // String page = initAttrWithDefault(model, "page", requestParams.get("page"),
  // Integer.toString(pageable.getPageNumber()));
  // String size = initAttrWithDefault(model, "size", requestParams.get("size"),
  // Integer.toString(pageable.getPageSize()));
  // String sort = initAttrWithDefault(model, "sort", requestParams.get("sort"),
  // Pageables.sortToParam(pageable.getSort()).get(0));
  //
  // return initPageable(model, PageRequest.of(Integer.valueOf(page),
  // Integer.valueOf(size), Pageables.paramToSort(sort)));
  // }
  //
  // public static Pageable initPageableWithDefault(Model model,
  // Map<String, String> requestParams, Pageable pageable,
  // HttpSession session) {
  // String page = initAttrWithDefault(model, "page", requestParams.get("page"),
  // Integer.toString(pageable.getPageNumber()), session);
  // String size = initAttrWithDefault(model, "size", requestParams.get("size"),
  // Integer.toString(pageable.getPageSize()), session);
  // String sort = initAttrWithDefault(model, "sort", requestParams.get("sort"),
  // Pageables.sortToParam(pageable.getSort()).get(0), session);
  //
  // return initPageable(model, PageRequest.of(Integer.valueOf(page),
  // Integer.valueOf(size), Pageables.paramToSort(sort)), session);
  // }
  //
  // public static Pageable initPageable(Model model, Pageable pageable) {
  // return initPageable(model, pageable, null);
  // }
  //
  // public static Pageable initPageable(Model model, Pageable pageable,
  // HttpSession session) {
  // return initAttrWithDefault(model, "pageable", pageable,
  // PageRequest.of(0, 10), session);
  // }
  //
  // public static <E> E initAttrWithDefault(Model model, String key, E value,
  // E defaultVal) {
  // return initAttrWithDefault(model, key, value, defaultVal, null);
  // }
  //
  // @SuppressWarnings("unchecked")
  // public static <E> E initAttrWithDefault(Model model, String key, E value,
  // E defaultVal, HttpSession session) {
  // if (value == null && session != null) {
  // value = (E) session.getAttribute(key);
  // }
  // if (value == null) value = defaultVal;
  //
  // model.addAttribute(key, value);
  // if (session != null) session.setAttribute(key, value);
  //
  // return value;
  // }
  //
  // public static <E> E initAttr(Model model, Map<String, String>
  // requestParams,
  // String key, E value) {
  // return initAttr(model, requestParams, key, value, null);
  // }
  //
  // @SuppressWarnings("unchecked")
  // public static <E> E initAttr(Model model, Map<String, String>
  // requestParams,
  // String key, E value, HttpSession session) {
  // if (!requestParams.containsKey(key)) {
  // if (session != null && session.getAttribute(key) != null) {
  // value = (E) session.getAttribute(key);
  // }
  // }
  //
  // model.addAttribute(key, value);
  // if (session != null) session.setAttribute(key, value);
  //
  // return value;
  // }

}
