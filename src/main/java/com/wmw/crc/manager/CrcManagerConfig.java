/*
 *
 * Copyright 2017 Wei-Ming Wu
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
package com.wmw.crc.manager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.datatables.repository.DataTablesRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.thymeleaf.dialect.springdata.SpringDataDialect;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.github.wnameless.spring.boot.up.web.Pageables;
import com.github.wnameless.thymeleaf.ext.Bootstrap3DropdownDialect;

@EnableJpaRepositories(
    repositoryFactoryBeanClass = DataTablesRepositoryFactoryBean.class)
@EnableJpaAuditing
@EnableScheduling
@ComponentScan("com.github.wnameless")
@Configuration
public class CrcManagerConfig {

  public static final String CASES_STATUS = "CASES_STATUS";

  @SuppressWarnings("deprecation")
  @Primary
  @Bean
  Jackson2ObjectMapperBuilder objectMapperBuilder() {
    Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
    // For CSIS API compatibility
    builder.featuresToEnable(Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
    return builder;
  }

  @Bean
  SpringDataDialect springDataDialect() {
    return new SpringDataDialect();
  }

  @Bean
  public Bootstrap3DropdownDialect bootstrap3DropdownDialect() {
    return new Bootstrap3DropdownDialect();
  }

  @Bean
  Pageables pageables() {
    return Pageables.getInstance();
  }

}
