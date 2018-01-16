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
package com.wmw.crc.manager.account.service;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.wmw.crc.manager.account.model.Role;
import com.wmw.crc.manager.account.model.User;
import com.wmw.crc.manager.account.repository.UserRepository;

@Component
public class JdbcAuthenticationProvider implements AuthenticationProvider {

  @Autowired
  private UserRepository userRepo;

  @Override
  public Authentication authenticate(Authentication authentication)
      throws AuthenticationException {
    String email = authentication.getName();
    Object credentials = authentication.getCredentials();

    User user = userRepo.findByEmail(email);
    if (credentials == null || user == null
        || !Objects.equals(user.getPassword(), credentials.toString())) {
      throw new AuthenticationException("Bad credentials") {

        private static final long serialVersionUID = 1L;

      };
    }

    List<GrantedAuthority> grantedAuths = newArrayList();
    for (Role role : user.getRoles()) {
      grantedAuths.add(new SimpleGrantedAuthority(role.getName()));
    }

    Authentication auth = new UsernamePasswordAuthenticationToken(
        user.getEmail(), user.getPassword(), grantedAuths);

    return auth;
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return authentication.equals(UsernamePasswordAuthenticationToken.class);
  }

}
