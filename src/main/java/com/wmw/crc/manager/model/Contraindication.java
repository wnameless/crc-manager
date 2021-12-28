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
package com.wmw.crc.manager.model;

import static javax.persistence.FetchType.EAGER;

import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;

import com.github.wnameless.spring.boot.up.web.RestfulItem;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false, of = { "id" })
@Data
@Entity
public class Contraindication implements RestfulItem<Long> {

  @Id
  @GeneratedValue
  Long id;

  Integer bundle;

  String phrase;

  @ElementCollection(fetch = EAGER)
  List<String> takekinds;

  String memo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinTable(name = "case_contraindication",
      joinColumns = { @JoinColumn(name = "contraindication_id") },
      inverseJoinColumns = { @JoinColumn(name = "case_id") })
  CaseStudy caseStudy;

  @Override
  public String getIndexPath() {
    return "/" + "contraindications";
  }

}
