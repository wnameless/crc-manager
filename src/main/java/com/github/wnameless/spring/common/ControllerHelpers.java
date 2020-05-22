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
import org.springframework.data.repository.CrudRepository;
import org.springframework.ui.Model;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ControllerHelpers {

  private ControllerHelpers() {}

  public static Pageable initPageable(Map<String, String> requestParams,
      Model model, HttpSession session) {
    String page = (String) initParamWithDefault("page",
        requestParams.get("page"), "0", model, session);
    String size = (String) initParamWithDefault("size",
        requestParams.get("size"), "10", model, session);
    String sort = (String) initParamWithDefault("sort",
        requestParams.get("sort"), "", model, session);

    return initPageable(PageRequest.of(Integer.valueOf(page),
        Integer.valueOf(size), PageUtils.paramToSort(sort)), model, session);
  }

  public static Pageable initPageableWithDefault(
      Map<String, String> requestParams, Model model, HttpSession session,
      Pageable pageable) {
    String page =
        (String) initParamWithDefault("page", requestParams.get("page"),
            Integer.toString(pageable.getPageNumber()), model, session);
    String size =
        (String) initParamWithDefault("size", requestParams.get("size"),
            Integer.toString(pageable.getPageSize()), model, session);
    String sort =
        (String) initParamWithDefault("sort", requestParams.get("sort"),
            PageUtils.sortToParam(pageable.getSort()).get(0), model, session);

    return initPageable(PageRequest.of(Integer.valueOf(page),
        Integer.valueOf(size), PageUtils.paramToSort(sort)), model, session);
  }

  public static Pageable initPageable(Pageable pageable, Model model) {
    return initPageable(pageable, model, null);
  }

  public static Pageable initPageable(Pageable pageable, Model model,
      HttpSession session) {
    return (Pageable) initParamWithDefault("pageable", pageable,
        PageRequest.of(0, 10), model, session);
  }

  public static Object initParamWithDefault(String key, Object value,
      Object defaultVal, Model model) {
    return initParamWithDefault(key, value, defaultVal, model, null);
  }

  public static Object initParamWithDefault(String key, Object value,
      Object defaultVal, Model model, HttpSession session) {
    log.info("key: " + key);
    log.info("value: " + value);
    log.info("defaultVal: " + defaultVal);
    log.info("model: " + model);
    log.info("session: " + session);

    if (value == null && session != null) {
      value = session.getAttribute(key);
    }
    if (value == null) value = defaultVal;

    if (model != null && key != null) model.addAttribute(key, value);
    if (session != null) session.setAttribute(key, value);

    return value;
  }

  public static Object initParam(Map<String, String> requestParams, String key,
      Object value, Model model) {
    return initParam(requestParams, key, value, model, null);
  }

  public static Object initParam(Map<String, String> requestParams, String key,
      Object value, Model model, HttpSession session) {
    if (!requestParams.containsKey(key)) {
      if (session != null && session.getAttribute(key) != null) {
        value = session.getAttribute(key);
      }
    }
    model.addAttribute(key, value);
    if (session != null) session.setAttribute(key, value);
    return value;
  }

  public static <I, ID> I initResourceItem(CrudRepository<I, ID> repo, ID id) {
    if (id == null) return null;
    return repo.findById(id).get();
  }

}
