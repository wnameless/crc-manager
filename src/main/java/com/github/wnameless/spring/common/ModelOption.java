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

  private boolean init = true;

  private Function<I, I> afterInitAction;

  private Function<I, I> preSetAction;

  public ModelOption<I> enable() {
    init = true;
    return this;
  }

  public ModelOption<I> disable() {
    init = false;
    return this;
  }

  public ModelOption<I> afterInitAction(Function<I, I> afterInitAction) {
    this.afterInitAction = afterInitAction;
    return this;
  }

  public ModelOption<I> preSetAction(Function<I, I> preSetAction) {
    this.preSetAction = preSetAction;
    return this;
  }

  boolean isInit() {
    return init;
  }

  Function<I, I> getAfterInitAction() {
    return afterInitAction;
  }

  Function<I, I> getPreSetAction() {
    return preSetAction;
  }

}