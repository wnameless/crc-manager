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

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.javers.core.metamodel.annotation.DiffIgnore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.jpa.type.flattenedjson.FlattenedJsonTypeConfigurer;
import com.github.wnameless.jpa.type.flattenedjson.JsonNodeConverter;
import com.github.wnameless.json.JsonPopulatable;
import com.github.wnameless.json.JsonPopulatedKey;
import com.github.wnameless.spring.react.ReactJsonSchemaForm;
import com.google.common.io.Resources;
import com.wmw.crc.manager.JsonSchemaPath;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false, of = { "id" })
@Data
@Table(indexes = { //
    @Index(columnList = "irbNumber", unique = false),
    @Index(columnList = "trialName", unique = false),
    @Index(columnList = "piName", unique = false),
    @Index(columnList = "expectedStartDate", unique = false),
    @Index(columnList = "expectedEndDate", unique = false) })
@Entity
public class CaseStudy implements JsonPopulatable, ReactJsonSchemaForm {

  public static final JsonNode SCHEMA;
  public static final JsonNode UI_SCHEMA;
  static {
    URL url = Resources.getResource(JsonSchemaPath.applicationSchema);
    JsonNode jsonNode = null;
    try {
      jsonNode = new ObjectMapper().readTree(Resources.toString(url, UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
    SCHEMA = jsonNode;
    url = Resources.getResource(JsonSchemaPath.applicationUISchema);
    try {
      jsonNode = new ObjectMapper().readTree(Resources.toString(url, UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
    UI_SCHEMA = jsonNode;
  }

  @Convert(converter = JsonNodeConverter.class)
  @Column(columnDefinition = "text")
  protected JsonNode formData = FlattenedJsonTypeConfigurer.INSTANCE
      .getObjectMapperFactory().get().createObjectNode();

  // @Convert(converter = JsonNodeConverter.class)
  // @Column(columnDefinition = "text")
  // protected JsonNode schema = FlattenedJsonTypeConfigurer.INSTANCE
  // .getObjectMapperFactory().get().createObjectNode();
  //
  // @Convert(converter = JsonNodeConverter.class)
  // @Column(columnDefinition = "text")
  // protected JsonNode uiSchema = FlattenedJsonTypeConfigurer.INSTANCE
  // .getObjectMapperFactory().get().createObjectNode();

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

  @JsonPopulatedKey("irbNum")
  String irbNumber;

  @JsonPopulatedKey("protocolNum")
  String caseNumber;

  @JsonPopulatedKey("title")
  String trialName;

  @JsonPopulatedKey("engTitle")
  String trialNameEng;

  @JsonPopulatedKey("PI[0].name")
  String piName;

  @JsonPopulatedKey("PI[0].email1")
  String piEmail1;

  @JsonPopulatedKey("PI[0].email2")
  String piEmail2;

  @JsonPopulatedKey("crcNum")
  String projectNumber;

  @JsonPopulatedKey("nihReason")
  String projectType;

  int expectedNumberOfSubjectsLocal;

  int expectedNumberOfSubjectsNational;

  int expectedNumberOfSubjectsGlobal;

  @JsonPopulatedKey("proposedStartDate")
  String expectedStartDate;

  @JsonPopulatedKey("proposedEndDate")
  String expectedEndDate;

  @Enumerated(EnumType.STRING)
  Status status = Status.NEW;

  @DiffIgnore
  @OneToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "case_subject", joinColumns = @JoinColumn(name = "case_id"),
      inverseJoinColumns = @JoinColumn(name = "subject_id"))
  List<Subject> subjects = newArrayList();

  @DiffIgnore
  @OneToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "case_contraindication",
      joinColumns = @JoinColumn(name = "case_id"),
      inverseJoinColumns = @JoinColumn(name = "contraindication_id"))
  List<Contraindication> contraindications = newArrayList();

  // Permission
  String owner;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "case_managers",
      joinColumns = @JoinColumn(name = "case_id"))
  Set<String> managers = newLinkedHashSet();

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "case_editors",
      joinColumns = @JoinColumn(name = "case_id"))
  Set<String> editors = newLinkedHashSet();

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "case_viewers",
      joinColumns = @JoinColumn(name = "case_id"))
  Set<String> viewers = newLinkedHashSet();

  public CaseStudy() {
    // ObjectMapper mapper = new ObjectMapper();
    //
    // try {
    // URL url = Resources.getResource(JsonSchemaPath.applicationSchema);
    // setSchema(mapper.readTree(Resources.toString(url, UTF_8)));
    // url = Resources.getResource(JsonSchemaPath.applicationUISchema);
    // setUiSchema(mapper.readTree(Resources.toString(url, UTF_8)));
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
  }

  @Override
  public void setFormData(JsonNode formData) {
    this.formData = formData;
    try {
      setPopulatedJson(new ObjectMapper().writeValueAsString(formData));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public JsonNode getSchema() {
    return SCHEMA;
  }

  @Override
  public JsonNode getUiSchema() {
    return UI_SCHEMA;
  }

  @Override
  public void setSchema(JsonNode schema) {}

  @Override
  public void setUiSchema(JsonNode uiSchema) {}

}
