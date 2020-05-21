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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

public class PageUtils {

  public static String toQueryString(Pageable pageable) {
    if (pageable == null) return "";

    URIBuilder b = new URIBuilder();
    b.addParameter("page", String.valueOf(pageable.getPageNumber()));
    b.addParameter("size", String.valueOf(pageable.getPageSize()));
    pageable.getSort().forEach(order -> {
      b.addParameter("sort",
          "" + order.getProperty() + "," + order.getDirection());
    });

    URI uri;
    try {
      uri = b.build();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    return uri.getQuery();
  }

  public static String toQueryStringWithoutPage(Pageable pageable) {
    if (pageable == null) return "";

    URIBuilder b = new URIBuilder();
    b.addParameter("size", String.valueOf(pageable.getPageSize()));
    pageable.getSort().forEach(order -> {
      b.addParameter("sort",
          "" + order.getProperty() + "," + order.getDirection());
    });

    URI uri;
    try {
      uri = b.build();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    return uri.getQuery();
  }

  public static List<String> sortToParam(Sort sort) {
    return sort.stream().map(o -> o.getProperty() + "," + o.getDirection())
        .collect(Collectors.toList());
  }

  public static Sort paramToSort(String... params) {
    if (params.length == 0) return Sort.unsorted();

    List<Order> orderList = new ArrayList<>();

    for (String param : params) {
      String[] propAndDerct = param.split(",");
      if (propAndDerct.length == 1) {
        orderList.add(Order.by(propAndDerct[0]));
      } else if (propAndDerct.length == 2) {
        Direction direction;
        try {
          direction = Direction.fromString(propAndDerct[1]);
          if (direction == Direction.ASC) {
            orderList.add(Order.asc(propAndDerct[0]));
          }
          if (direction == Direction.DESC) {
            orderList.add(Order.desc(propAndDerct[0]));
          }
        } catch (Exception e) {
          orderList.add(Order.by(propAndDerct[0]));
        }
      }
    }

    return Sort.by(orderList);
  }

}
