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

import java.util.function.Function;

public class ModelOption<I> {

  private boolean enable = true;

  private Function<I, I> afterInit;

  private Function<I, I> beforeAdd;

  public ModelOption<I> enable() {
    enable = true;
    return this;
  }

  public ModelOption<I> disable() {
    enable = false;
    return this;
  }

  public ModelOption<I> afterInit(Function<I, I> afterInit) {
    this.afterInit = afterInit;
    return this;
  }

  public ModelOption<I> beforeAdd(Function<I, I> beforeAdd) {
    this.beforeAdd = beforeAdd;
    return this;
  }

  boolean isEnable() {
    return enable;
  }

  Function<I, I> getAfterInit() {
    return afterInit;
  }

  Function<I, I> getBeforeAdd() {
    return beforeAdd;
  }

}
