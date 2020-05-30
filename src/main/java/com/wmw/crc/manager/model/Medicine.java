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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.github.wnameless.spring.common.web.RestfulItem;
import com.wmw.crc.manager.RestfulPath;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false, of = { "id" })
@Data
@Entity
public class Medicine implements RestfulItem<Long> {

  @Id
  @GeneratedValue
  Long id;

  String name;

  String engName;

  String scientificName;

  String hospitalCode;

  String atcCode1;

  String atcCode2;

  String atcCode3;

  String atcCode4;

  String takekind;

  @Override
  public String getIndexPath() {
    return "/" + RestfulPath.Names.MEDICINE;
  }

}
