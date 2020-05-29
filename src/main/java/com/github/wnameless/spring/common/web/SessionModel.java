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

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;

import com.github.wnameless.spring.common.web.Pageables.PageableParams;

public final class SessionModel {

  public static SessionModel of(Model model, HttpSession session) {
    return new SessionModel(model, session);
  }

  private final Model model;
  private final HttpSession session;

  public SessionModel(Model model, HttpSession session) {
    if (model == null) throw new NullPointerException();
    if (session == null) throw new NullPointerException();

    this.model = model;
    this.session = session;
  }

  public <E> E initAttr(String key, E value) {
    return initAttr(key, value, null);
  }

  @SuppressWarnings("unchecked")
  public <E> E initAttr(String key, E value, E defaultVal) {
    if (value == null) {
      value = (E) session.getAttribute(key);
    }
    if (value == null) value = defaultVal;

    model.addAttribute(key, value);
    session.setAttribute(key, value);

    return value;
  }

  @SuppressWarnings("unchecked")
  public <E> E initAttr(String key, E value, boolean skipSessionLookUp) {
    if (!skipSessionLookUp) {
      value = (E) session.getAttribute(key);
    }

    model.addAttribute(key, value);
    session.setAttribute(key, value);

    return value;
  }

  public Pageable initPageable(Map<String, String> requestParams) {
    return initPageable(requestParams, PageableParams.ofSpring());
  }

  public Pageable initPageable(Map<String, String> requestParams,
      PageableParams pageableParams) {
    String pageParam = pageableParams.getPageParameter();
    String sizeParam = pageableParams.getSizeParameter();
    String sortParam = pageableParams.getSortParameter();
    String page = initAttr(pageParam, requestParams.get(pageParam), "0");
    String size = initAttr(sizeParam, requestParams.get(sizeParam), "10");
    String sort = initAttr(sortParam, requestParams.get(sortParam), "");

    return initAttr("pageable", PageRequest.of(Integer.valueOf(page),
        Integer.valueOf(size), Pageables.paramToSort(sort)));
  }

  public Pageable initPageable(Map<String, String> requestParams,
      Pageable defaultPageable) {
    String page = initAttr("page", requestParams.get("page"),
        Integer.toString(defaultPageable.getPageNumber()));
    String size = initAttr("size", requestParams.get("size"),
        Integer.toString(defaultPageable.getPageSize()));
    String sort = initAttr("sort", requestParams.get("sort"),
        Pageables.sortToParam(defaultPageable.getSort()).get(0));

    return initAttr("pageable", PageRequest.of(Integer.valueOf(page),
        Integer.valueOf(size), Pageables.paramToSort(sort)));
  }

}
