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

import com.github.wnameless.spring.common.RestfulResource;

public enum RestfulModel implements RestfulResource {

  CASE_STUDY(Names.CASE_STUDY, CaseStudy.class),
  SUBJECT(Names.SUBJECT, Subject.class);

  private String resourceName;

  private Class<?> klass;

  private RestfulModel(String resourceName, Class<?> klass) {
    this.resourceName = resourceName;
    this.klass = klass;
  }

  @Override
  public String getResourceName() {
    return resourceName;
  }

  @Override
  public Class<?> getClassName() {
    return klass;
  }

  public static class Names {

    public static final String CASE_STUDY = "cases";

    public static final String SUBJECT = "subjects";

  }

}