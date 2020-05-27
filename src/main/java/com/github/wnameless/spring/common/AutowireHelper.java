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

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 
 * {@link AutowireHelper} is designed to retrieve the bean autowiring ability of
 * the {@link ApplicationContext} out from the Spring controlled IOC
 * environment. Simply by calling {@link #autowire(Object, Object...)} from this
 * singleton instance to autowire any instance. <br>
 * <br>
 * However, before {@link #autowire(Object, Object...)} gets work, user should
 * set or autowire the {@link ApplicationContext} first. <br>
 * <br>
 * For example:<br>
 * 
 * <pre>
 * &#64;Configuration
 * public class Config {
 * 
 *   &#64;Bean
 *   AutowireHelper autowireHelper() {
 *     return AutowireHelper.getInstance();
 *   }
 * 
 * }
 * </pre>
 * 
 * @author Wei-Ming Wu
 *
 */
public final class AutowireHelper implements ApplicationContextAware {

  private static final AutowireHelper INSTANCE = new AutowireHelper();
  private static ApplicationContext applicationContext;

  private AutowireHelper() {}

  /**
   * Tries to autowire target beans which hold
   * {@link org.springframework.beans.factory.annotation.Autowired @Autowired}
   * annotations in the given instance.
   *
   * @param instance
   *          of a class which holds
   *          {@link org.springframework.beans.factory.annotation.Autowired @Autowired}
   *          annotations
   * @param targetBeans
   *          which are annotated by
   *          {@link org.springframework.beans.factory.annotation.Autowired @Autowired}
   *          and not autowired yet in the given instance
   */
  public static void autowire(Object instance, Object... targetBeans) {
    for (Object bean : targetBeans) {
      if (bean == null) {
        applicationContext.getAutowireCapableBeanFactory()
            .autowireBean(instance);
      }
    }
  }

  @Override
  public void setApplicationContext(
      final ApplicationContext applicationContext) {
    AutowireHelper.applicationContext = applicationContext;
  }

  /**
   * @return the singleton {@link AutowireHelper}
   */
  public static AutowireHelper getInstance() {
    return INSTANCE;
  }

}