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

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

import com.github.wnameless.json.JsonDataInitailizable;
import com.github.wnameless.json.JsonInitKey;
import com.github.wnameless.spring.json.schema.form.JpaJsonSchemaForm;
import com.google.common.io.Resources;
import com.wmw.crc.manager.JsonSchemaPath;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false, of = { "id" })
@Data
@Entity(name = "kase")
public class Case extends JpaJsonSchemaForm implements JsonDataInitailizable {

  public enum Status {

    NEW, EXEC, END, NONE;

    public static Status fromString(String status) {
      switch (status.toUpperCase()) {
        case "NEW":
          return NEW;
        case "EXEC":
          return EXEC;
        case "END":
          return END;
        case "NONE":
          return NONE;
        default:
          return NEW;
      }
    }

  }

  @Id
  @GeneratedValue
  Long id;

  @JsonInitKey("irbNum")
  String irbNumber;

  @JsonInitKey("protocolNum")
  String caseNumber;

  @JsonInitKey("title")
  String trialName;

  @JsonInitKey("engTitle")
  String trialNameEng;

  @JsonInitKey("PI[0].name")
  String piName;

  String coPiName;

  String associatePiName;

  @JsonInitKey("crcNum")
  String projectNumber;

  @JsonInitKey("nihReason")
  String projectType;

  int expectedNumberOfSubjectsLocal;

  int expectedNumberOfSubjectsNational;

  int expectedNumberOfSubjectsGlobal;

  @JsonInitKey("proposedStartDate")
  String expectedStartDate;

  @JsonInitKey("proposedEndDate")
  String expectedEndDate;

  @Enumerated(EnumType.STRING)
  Status status = Status.NEW;

  @OneToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "case_subject", joinColumns = @JoinColumn(name = "case_id"),
      inverseJoinColumns = @JoinColumn(name = "subject_id"))
  List<Subject> subjects = newArrayList();

  @OneToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "case_contraindication",
      joinColumns = @JoinColumn(name = "case_id"),
      inverseJoinColumns = @JoinColumn(name = "contraindication_id"))
  List<Contraindication> contraindications = newArrayList();

  // Permission
  String owner;
  @ElementCollection
  @CollectionTable(name = "case_managers",
      joinColumns = @JoinColumn(name = "case_id"))
  Set<String> managers = newLinkedHashSet();
  @ElementCollection
  @CollectionTable(name = "case_editors",
      joinColumns = @JoinColumn(name = "case_id"))
  Set<String> editors = newLinkedHashSet();
  @ElementCollection
  @CollectionTable(name = "case_viewers",
      joinColumns = @JoinColumn(name = "case_id"))
  Set<String> viewers = newLinkedHashSet();

  public Case() {
    try {
      URL url = Resources.getResource(JsonSchemaPath.applicationSchema);
      setJsonSchema(Resources.toString(url, UTF_8));
      url = Resources.getResource(JsonSchemaPath.applicationUISchema);
      setJsonUiSchema(Resources.toString(url, UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void setJsonData(String jsonData) {
    super.setJsonData(jsonData);
    setJsonInitData(jsonData);
  }

}
