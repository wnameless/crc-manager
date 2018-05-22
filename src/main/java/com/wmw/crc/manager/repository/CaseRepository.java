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
package com.wmw.crc.manager.repository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Repository;

import com.wmw.crc.manager.model.Case;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {

  default List<Case> findByUserAndStatus(Authentication auth,
      Case.Status status) {
    Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
    if (authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
        || authorities.contains(new SimpleGrantedAuthority("ROLE_SUPER"))) {
      return findByStatus(status);
    }

    String username = auth.getName();
    return findByStatusAndOwnerEqualsOrManagersContainsOrEditorsContainsOrViewersContains(
        status, username, username, username, username);
  }

  List<Case> findByStatusAndOwnerEqualsOrManagersContainsOrEditorsContainsOrViewersContains(
      Case.Status status, String username1, String username2, String username3,
      String username4);

  List<Case> findByStatus(Case.Status status);

  Case findByIrbNumber(String irbNumber);

  default List<Case> findAllUndoneCrc() {
    return findAll().stream()
        .filter(c -> c.isFormDone() && !c.getCrc().isFormDone())
        .collect(Collectors.toList());
  }

}
