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
package com.wmw.crc.manager.model;

import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

import com.wmw.crc.manager.account.model.User;

import lombok.Data;

@Data
@Entity
public class PermissionGroup {

  @Id
  @GeneratedValue
  long id;

  @OneToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "permission_group_read",
      joinColumns = @JoinColumn(name = "case_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  Set<User> readers = newLinkedHashSet();

  @OneToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "permission_group_write",
      joinColumns = @JoinColumn(name = "case_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  Set<User> writers = newLinkedHashSet();

  @OneToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "permission_group_submit",
      joinColumns = @JoinColumn(name = "case_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  Set<User> submitters = newLinkedHashSet();

  @OneToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "permission_group_manage",
      joinColumns = @JoinColumn(name = "case_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  Set<User> managers = newLinkedHashSet();

}
