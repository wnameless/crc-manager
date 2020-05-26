/*
 *
 * Copyright 2019 Wei-Ming Wu
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
package com.wmw.crc.manager.model;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.github.wnameless.spring.common.RestfulItem;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false, of = { "id" })
@Data
@Entity
public class Visit extends Auditable<String> implements RestfulItem<Long> {

  @Id
  @GeneratedValue
  Long id;

  @ManyToOne // (cascade = CascadeType.MERGE)
  @JoinColumn(name = "subject_id")
  Subject subject;

  String nationalId;

  String division;

  String doctor;

  String room;

  LocalDate date;

  boolean contraindicationSuspected;

  String irbNumber;

  boolean reviewed;

  @Override
  public String getIndexPath() {
    return "/" + RestfulModel.Names.VISIT;
  }

}
