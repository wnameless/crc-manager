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
package com.wmw.crc.manager.util;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component("pageUtils")
public class PageUtils {

  public String toQueryString(Pageable pageable) {
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

  public String toQueryStringWithoutPage(Pageable pageable) {
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

}
