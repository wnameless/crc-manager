/*
 *
 * Copyright 2020 Wei-Ming Wu
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.sf.rubycollect4j.Ruby;
import net.sf.rubycollect4j.RubyArray;

@EqualsAndHashCode(callSuper = false, of = { "id" })
@Data
@Table(indexes = { //
    @Index(columnList = "id,jsonKey", unique = true) })
@Entity
public class JsonDataUriFile {

  @Id
  @GeneratedValue
  Long id;

  @ManyToOne
  @JoinTable(name = "case_json_data_uri_file",
      joinColumns = { @JoinColumn(name = "json_data_uri_file_id") },
      inverseJoinColumns = { @JoinColumn(name = "case_id") })
  CaseStudy caseStudy;

  @Column(nullable = false)
  String jsonKey;

  String mediaType;

  String parameters;

  @Column(columnDefinition = "text")
  String data;

  public JsonDataUriFile() {}

  public JsonDataUriFile(CaseStudy caseStudy, String jsonKey, String dataUri) {
    this.caseStudy = caseStudy;
    this.jsonKey = jsonKey;

    dataUri = dataUri.replaceFirst("^data:", "");

    String[] schema = dataUri.split(",");
    String[] mediaTypeParameters = schema[0].split(";");

    String mediaType = mediaTypeParameters[0];
    this.mediaType = mediaType;

    RubyArray<String> parameters = Ruby.Array.copyOf(mediaTypeParameters);
    parameters.shift();
    this.parameters = parameters.join(";");

    String data = schema.length > 1 ? schema[1] : "";
    this.data = data;
  }

}
