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
package com.wmw.crc.manager.model;

import com.github.wnameless.spring.common.RestfulRoute;

public enum RestfulModel implements RestfulRoute<Long> {

  CASE_STUDY(Names.CASE_STUDY), SUBJECT(Names.SUBJECT);

  private final String resourceName;

  private RestfulModel(String resourceName) {
    this.resourceName = resourceName;
  }

  @Override
  public String getIndexPath() {
    return "/" + resourceName;
  }

  public static class Names {

    public static final String CASE_STUDY = "cases";

    public static final String SUBJECT = "subjects";

  }

}
