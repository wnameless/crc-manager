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

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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
public class Case {

  @Id
  @GeneratedValue
  long id;

  int year;

  String caseNumber;

  String trialName;

  String trialNameEng;

  String piName;

  String coPiName;

  String associatePiName;

  String projectNumber;

  String projectType;

  int expectedNumberOfSubjectsLocal;

  int expectedNumberOfSubjectsNational;

  int expectedNumberOfSubjectsGlobal;

  Date expectedStartDate;

  Date expectedEndDate;

  @Column(columnDefinition = "TEXT")
  String formJsonData;

  boolean formDone;

  @Column(columnDefinition = "TEXT")
  String formSupplement1JsonData;

  boolean formSupplement1Done;

  @Column(columnDefinition = "TEXT")
  String formSupplement2JsonData;

  boolean formSupplement2Done;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinTable(name = "case_subject", joinColumns = @JoinColumn(name = "case_id"),
      inverseJoinColumns = @JoinColumn(name = "subject_id"))
  Set<Subject> subjects = newLinkedHashSet();

  @OneToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "case_reader", joinColumns = @JoinColumn(name = "case_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  Set<User> readers = newLinkedHashSet();

  @OneToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "case_writer", joinColumns = @JoinColumn(name = "case_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  Set<User> writers = newLinkedHashSet();

  @OneToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "case_submitter",
      joinColumns = @JoinColumn(name = "case_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  Set<User> submitters = newLinkedHashSet();

  @OneToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "case_manager", joinColumns = @JoinColumn(name = "case_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  Set<User> managers = newLinkedHashSet();

}
