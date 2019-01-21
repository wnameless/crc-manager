/*
 *
 * Copyright 2018 Wei-Ming Wu
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

import javax.servlet.http.HttpServletRequest;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.keycloak.adapters.springsecurity.filter.QueryParamPresenceRequestMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
public class KeycloakConfig extends KeycloakWebSecurityConfigurerAdapter {

  @Bean
  @Override
  protected KeycloakAuthenticationProcessingFilter keycloakAuthenticationProcessingFilter()
      throws Exception {
    RequestMatcher requestMatcher =
        new OrRequestMatcher(new AntPathRequestMatcher("/sso/login"),
            new QueryParamPresenceRequestMatcher(OAuth2Constants.ACCESS_TOKEN),
            // We're providing our own authorization header matcher
            new IgnoreKeycloakProcessingFilterRequestMatcher());
    return new KeycloakAuthenticationProcessingFilter(
        authenticationManagerBean(), requestMatcher);
  }

  // Matches request with Authorization header which value doesn't start with
  // "Basic " prefix
  private class IgnoreKeycloakProcessingFilterRequestMatcher
      implements RequestMatcher {
    IgnoreKeycloakProcessingFilterRequestMatcher() {}

    public boolean matches(HttpServletRequest request) {
      String authorizationHeaderValue = request.getHeader("Authorization");
      return authorizationHeaderValue != null
          && !authorizationHeaderValue.startsWith("Basic ");
    }

  }

  // Submits the KeycloakAuthenticationProvider to the AuthenticationManager
  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth)
      throws Exception {
    KeycloakAuthenticationProvider keycloakAuthenticationProvider =
        keycloakAuthenticationProvider();
    keycloakAuthenticationProvider
        .setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
    auth.authenticationProvider(keycloakAuthenticationProvider);

    auth.inMemoryAuthentication().withUser("api_key").password("22711e4e")
        .roles("API");
  }

  @Bean
  public KeycloakSpringBootConfigResolver KeycloakConfigResolver() {
    return new KeycloakSpringBootConfigResolver();
  }

  // Specifies the session authentication strategy
  @Bean
  @Override
  protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
    return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    super.configure(http);
    http.authorizeRequests().antMatchers("/webjars/**").permitAll() //
        .antMatchers("/css/**").permitAll() //
        .antMatchers("/js/**").permitAll() //
        .antMatchers("/default/**").permitAll() //
        .antMatchers("/myfavicon.ico").permitAll() //
        .antMatchers("/api/**").permitAll() //
        // .antMatchers("/users/new").anonymous()
        // .antMatchers(HttpMethod.POST, "/users").anonymous() //
        .anyRequest().authenticated().and()// .formLogin().loginPage("/login")
        // .permitAll().and()
        .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
        .logoutSuccessUrl("/").permitAll();

    // http.authorizeRequests().antMatchers("/api/**").authenticated().and()
    // .httpBasic();
    http.csrf().disable();
    // http.authorizeRequests().antMatchers("/customers*").hasRole("user")
    // .anyRequest().permitAll();
  }

}
