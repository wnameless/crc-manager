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

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 
 * {@link AutowireHelper} is designed to retrieve the autowiring ability of the
 * {@link ApplicationContext} out from the Spring controlled IOC environment.
 * Simply by calling {@link #autowire(Object) AutowireHelper.autowire(Object)}
 * from this singleton instance to autowire any bean. <br>
 * <br>
 * However before {@link #autowire(Object) AutowireHelper.autowire(Object)} gets
 * work, user should set or autowire the {@link ApplicationContext} first. <br>
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
   * @see {@link AutowireCapableBeanFactory#autowireBean}
   */
  public static void autowire(Object instance) {
    applicationContext.getAutowireCapableBeanFactory().autowireBean(instance);
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