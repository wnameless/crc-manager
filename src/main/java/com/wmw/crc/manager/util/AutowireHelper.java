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

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public final class AutowireHelper implements ApplicationContextAware {

  private static final AutowireHelper INSTANCE = new AutowireHelper();
  private static ApplicationContext applicationContext;

  private AutowireHelper() {}

  /**
   * Tries to autowire the specified instance of the class if one of the
   * specified beans which need to be autowired are null.
   *
   * @param classToAutowire
   *          the instance of the class which holds @Autowire annotations
   * @param beansToAutowireInClass
   *          the beans which have the @Autowire annotation in the specified
   *          {#classToAutowire}
   */
  public static void autowire(Object classToAutowire,
      Object... beansToAutowireInClass) {
    for (Object bean : beansToAutowireInClass) {
      if (bean == null) {
        applicationContext.getAutowireCapableBeanFactory()
            .autowireBean(classToAutowire);
      }
    }
  }

  @Override
  public void setApplicationContext(
      final ApplicationContext applicationContext) {
    AutowireHelper.applicationContext = applicationContext;
  }

  /**
   * @return the singleton instance.
   */
  public static AutowireHelper getInstance() {
    return INSTANCE;
  }

}