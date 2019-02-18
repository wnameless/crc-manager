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
package com.wmw.crc.manager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import net.sf.rubycollect4j.Ruby;

@Service("perm")
public class CasePermissionService {

  @Autowired
  CaseStudyRepository caseRepo;

  public boolean isUser() {
    Authentication auth =
        SecurityContextHolder.getContext().getAuthentication();
    return auth.getAuthorities().contains(
        new SimpleGrantedAuthority("ROLE_USER")) || isAdmin() || isSuper();
  }

  public boolean canManage(CaseStudy kase) {
    Authentication auth =
        SecurityContextHolder.getContext().getAuthentication();
    return isSuper() || isAdmin() || isOwner(kase)
        || Ruby.Set.copyOf(kase.getManagers()).map(String::toLowerCase)
            .contains(auth.getName().toLowerCase());
  }

  public boolean canManage(Long caseId) {
    CaseStudy kase = caseRepo.getOne(caseId);
    return canManage(kase);
  }

  public boolean canRead(CaseStudy kase) {
    Authentication auth =
        SecurityContextHolder.getContext().getAuthentication();
    return isSuper() || isAdmin() || isOwner(kase)
        || Ruby.Set.copyOf(kase.getManagers()).map(String::toLowerCase)
            .contains(auth.getName().toLowerCase())
        || Ruby.Set.copyOf(kase.getViewers()).map(String::toLowerCase)
            .contains(auth.getName().toLowerCase())
        || Ruby.Set.copyOf(kase.getEditors()).map(String::toLowerCase)
            .contains(auth.getName().toLowerCase());
  }

  public boolean canRead(Long caseId) {
    CaseStudy kase = caseRepo.getOne(caseId);
    return canRead(kase);
  }

  public boolean canWrite(CaseStudy kase) {
    Authentication auth =
        SecurityContextHolder.getContext().getAuthentication();
    boolean isOpenCase = kase.getStatus() != CaseStudy.Status.END;
    return isSuper() //
        || isAdmin()//
        || (isOpenCase && (isOwner(kase)
            || Ruby.Set.copyOf(kase.getManagers()).map(String::toLowerCase)
                .contains(auth.getName().toLowerCase())
            || Ruby.Set.copyOf(kase.getEditors()).map(String::toLowerCase)
                .contains(auth.getName().toLowerCase())));
  }

  public boolean canWrite(Long caseId) {
    CaseStudy kase = caseRepo.getOne(caseId);
    return canWrite(kase);
  }

  public boolean canAssign() {
    return isSuper() || isAdmin();
  }

  public boolean canClose(CaseStudy kase) {
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

  public boolean canDeleteSubject(CaseStudy kase) {
    Authentication auth =
        SecurityContextHolder.getContext().getAuthentication();
    boolean isOpenCase = kase.getStatus() != CaseStudy.Status.END;
    return isSuper() //
        || isAdmin() //
        || (isOpenCase && (isOwner(kase) || Ruby.Set.copyOf(kase.getManagers())
            .map(String::toLowerCase).contains(auth.getName().toLowerCase())));
  }

  public boolean canDeleteSubject(Long caseId) {
    CaseStudy kase = caseRepo.getOne(caseId);
    return canDeleteSubject(kase);
  }

  public boolean isOwner(CaseStudy kase) {
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
