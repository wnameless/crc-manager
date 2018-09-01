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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.wmw.crc.manager.model.Case;
import com.wmw.crc.manager.repository.CaseRepository;

import net.sf.rubycollect4j.Ruby;

@Service("perm")
public class CasePermissionService {

  @Autowired
  CaseRepository caseRepo;

  public boolean isUser() {
    Authentication auth =
        SecurityContextHolder.getContext().getAuthentication();
    return auth.getAuthorities().contains(
        new SimpleGrantedAuthority("ROLE_USER")) || isAdmin() || isSuper();
  }

  public boolean canManage(Case kase) {
    Authentication auth =
        SecurityContextHolder.getContext().getAuthentication();
    return isSuper() || isAdmin() || isOwner(kase)
        || Ruby.Set.copyOf(kase.getManagers()).map(String::toUpperCase)
            .contains(auth.getName().toUpperCase());
  }

  public boolean canManage(Long caseId) {
    Case kase = caseRepo.findOne(caseId);
    return canManage(kase);
  }

  public boolean canRead(Case kase) {
    Authentication auth =
        SecurityContextHolder.getContext().getAuthentication();
    return isSuper() || isAdmin() || isOwner(kase)
        || Ruby.Set.copyOf(kase.getManagers()).map(String::toUpperCase)
            .contains(auth.getName().toUpperCase())
        || Ruby.Set.copyOf(kase.getViewers()).map(String::toUpperCase)
            .contains(auth.getName().toUpperCase())
        || Ruby.Set.copyOf(kase.getEditors()).map(String::toUpperCase)
            .contains(auth.getName().toUpperCase());
  }

  public boolean canRead(Long caseId) {
    Case kase = caseRepo.findOne(caseId);
    return canRead(kase);
  }

  public boolean canReadClosed(Case kase) {
    return isSuper() || isAdmin() || isOwner(kase);
  }

  public boolean canWrite(Case kase) {
    Authentication auth =
        SecurityContextHolder.getContext().getAuthentication();
    return isSuper() || isAdmin() || isOwner(kase)
        || Ruby.Set.copyOf(kase.getManagers()).map(String::toUpperCase)
            .contains(auth.getName().toUpperCase())
        || Ruby.Set.copyOf(kase.getEditors()).map(String::toUpperCase)
            .contains(auth.getName().toUpperCase());
  }

  public boolean canWrite(Long caseId) {
    Case kase = caseRepo.findOne(caseId);
    return canWrite(kase);
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

  public boolean canDelete() {
    return isSuper() || isAdmin();
  }

  public boolean canDeleteSubject(Case kase) {
    Authentication auth =
        SecurityContextHolder.getContext().getAuthentication();
    return isSuper() || isAdmin() || isOwner(kase)
        || Ruby.Set.copyOf(kase.getManagers()).map(String::toUpperCase)
            .contains(auth.getName().toUpperCase());
  }

  public boolean isOwner(Case kase) {
    if (kase.getOwner() == null) return false;

    Authentication auth =
        SecurityContextHolder.getContext().getAuthentication();
    return auth.getName().compareToIgnoreCase(kase.getOwner()) == 0;
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
