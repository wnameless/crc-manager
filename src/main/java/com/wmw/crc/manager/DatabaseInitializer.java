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

import static com.google.common.collect.Sets.newHashSet;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wmw.crc.manager.account.model.Role;
import com.wmw.crc.manager.account.model.User;
import com.wmw.crc.manager.account.repository.RoleRepository;
import com.wmw.crc.manager.account.repository.UserRepository;
import com.wmw.crc.manager.model.Case;
import com.wmw.crc.manager.repository.CaseRepository;

import net.sf.rubycollect4j.Ruby;

@Component
public class DatabaseInitializer {

  @Autowired
  UserRepository userRepo;

  @Autowired
  RoleRepository roleRepo;

  @Autowired
  CaseRepository caseRepo;

  @PostConstruct
  void init() {
    if (roleRepo.count() == 0) {
      roleRepo.save(
          Ruby.Array.of("ROLE_SUPER", "ROLE_ADMIN", "ROLE_USER").map(name -> {
            Role role = new Role();
            role.setName(name);
            return role;
          }));
    }

    User user = userRepo.findByUsername("super");
    if (user == null) {
      user = new User();
      user.setUsername("super");
      user.setEmail("super@crcmanager.com");
      user.setPassword("1qaz@WSX");
      user.setRoles(newHashSet(roleRepo.findByName("ROLE_SUPER")));
      userRepo.save(user);
    }

    while (caseRepo.count() < 10) {
      Case c = new Case();
      c.setYear(2019);
      c.setPiName("Jhon Doe");
      c.setCaseNumber("878787");
      c.setTrialName("萬靈藥");
      c.setTrialNameEng("Elixir");
      c.setPiName("冤大頭");
      c.setCoPiName("替死鬼");
      c.setAssociatePiName("鬼遮眼");
      c.setProjectNumber("HAHAHAH");
      c.setProjectType("Free");
      c.setExpectedNumberOfSubjectsLocal(100);
      c.setExpectedNumberOfSubjectsNational(1000);
      c.setExpectedNumberOfSubjectsGlobal(10000);
      c.setExpectedStartDate(Ruby.Date.today());
      c.setExpectedEndDate(Ruby.Date.today().add(300).days());
      caseRepo.save(c);
    }
  }

}
