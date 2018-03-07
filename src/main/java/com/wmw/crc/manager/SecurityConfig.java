/*
 *
 * Copyright 2018 Wei-Ming Wu
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.wmw.crc.manager.account.service.JdbcAuthenticationProvider;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  JdbcAuthenticationProvider authenticationProvider;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests().antMatchers("/webjars/**").permitAll() //
        .antMatchers("/css/**").permitAll() //
        .antMatchers("/js/**").permitAll() //
        .antMatchers("/default/**").permitAll() //
        .antMatchers("/myfavicon.ico").permitAll() //
        .antMatchers("/users/new").anonymous() //
        .antMatchers(HttpMethod.POST, "/users").anonymous() //
        .anyRequest().authenticated().and().formLogin().loginPage("/login")
        .permitAll().and().logout()
        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
        .logoutSuccessUrl("/login").permitAll();

    http.authorizeRequests().antMatchers("/api/**").authenticated().and()
        .httpBasic();
    http.csrf().disable();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    // auth.inMemoryAuthentication().withUser("jim").password("demo")
    // .roles("ADMIN").and().withUser("bob").password("demo").roles("USER")
    // .and().withUser("ted").password("demo").roles("USER", "ADMIN");
    auth.authenticationProvider(authenticationProvider);
  }

}
