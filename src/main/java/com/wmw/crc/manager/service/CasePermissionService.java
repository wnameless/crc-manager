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
package com.wmw.crc.manager.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.wmw.crc.manager.model.Case;

@Service("perm")
public class CasePermissionService {

  public boolean canManage(Case kase) {
    Authentication auth =
        SecurityContextHolder.getContext().getAuthentication();
    return isSuper() || isAdmin() || isOwner(kase)
        || kase.getManagers().contains(auth.getName());
  }

  public boolean canRead(Case kase) {
    Authentication auth =
        SecurityContextHolder.getContext().getAuthentication();
    return isSuper() || isAdmin() || isOwner(kase)
        || kase.getViewers().contains(auth.getName())
        || kase.getEditors().contains(auth.getName());
  }

  public boolean canWrite(Case kase) {
    Authentication auth =
        SecurityContextHolder.getContext().getAuthentication();
    return isSuper() || isAdmin() || isOwner(kase)
        || kase.getEditors().contains(auth.getName());
  }

  public boolean canAssign() {
    return isSuper() || isAdmin();
  }

  public boolean canClose(Case kase) {
    return isSuper() || isAdmin() || isOwner(kase);
  }

  public boolean canRenew() {
    return isSuper() || isAdmin();
  }

  public boolean canReopen() {
    return isSuper() || isAdmin();
  }

  public boolean isOwner(Case kase) {
    Authentication auth =
        SecurityContextHolder.getContext().getAuthentication();
    return auth.getName().equals(kase.getOwner());
  }

  public boolean isAdmin() {
    Authentication auth =
        SecurityContextHolder.getContext().getAuthentication();
    return auth.getAuthorities()
        .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
  }

  public boolean isSuper() {
    Authentication auth =
        SecurityContextHolder.getContext().getAuthentication();
    return auth.getAuthorities()
        .contains(new SimpleGrantedAuthority("ROLE_SUPER"));
  }

}
