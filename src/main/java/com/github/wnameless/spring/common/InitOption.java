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

public class InitOption<I> {

  private boolean init = true;

  private Function<I, I> afterAction;

  public InitOption<I> enable() {
    init = true;
    return this;
  }

  public InitOption<I> disable() {
    init = false;
    return this;
  }

  public InitOption<I> afterAction(Function<I, I> itemConsumer) {
    this.afterAction = itemConsumer;
    return this;
  }

  boolean isInit() {
    return init;
  }

  Function<I, I> getAfterAction() {
    return afterAction;
  }

}
