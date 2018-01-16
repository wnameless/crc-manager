/*
 *
 * Copyright 2017 Wei-Ming Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.wmw.crc.manager;

import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class ServiceConfig {

  @Bean
  EmbeddedServletContainerCustomizer containerCustomizer() {
    return (container) -> {
      ErrorPage error401Page =
          new ErrorPage(HttpStatus.UNAUTHORIZED, "/401.html");
      ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/404.html");
      ErrorPage error500Page =
          new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/500.html");
      container.addErrorPages(error401Page, error404Page, error500Page);
    };
  }

}