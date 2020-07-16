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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.javers.core.metamodel.annotation.DiffIgnore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.wnameless.jpa.type.flattenedjson.FlattenedJsonTypeConfigurer;
import com.github.wnameless.jpa.type.flattenedjson.JsonNodeConverter;
import com.github.wnameless.json.beanpopulator.JsonPopulatable;
import com.github.wnameless.json.beanpopulator.JsonPopulatedKey;
import com.github.wnameless.json.beanpopulator.JsonPopulatedValue;
import com.github.wnameless.spring.common.web.RestfulItem;
import com.github.wnameless.spring.react.jsf.ReactJsonSchemaForm;
import com.wmw.crc.manager.JsonSchema;
import com.wmw.crc.manager.RestfulPath;
import com.wmw.crc.manager.util.SubjectStatusCustomizer;

import lombok.Data;
import lombok.EqualsAndHashCode;

//@EntityListeners(SubjectEntityListener.class)
@EqualsAndHashCode(callSuper = false, of = { "id" })
@Data
@Entity
public class Subject
    implements JsonPopulatable, ReactJsonSchemaForm, RestfulItem<Long> {

  @Override
  public String getIndexPath() {
    return "/" + RestfulPath.Names.SUBJECT;
  }

  @Id
  @GeneratedValue
  Long id;

  @JsonPopulatedValue(SubjectStatusCustomizer.class)
  Status status = Status.PRESCREENING;

  @JsonPopulatedKey("lastname")
  String name;

  @JsonPopulatedKey("taiwanId")
  String nationalId;

  @JsonPopulatedKey("mrn")
  String patientId;

  @JsonPopulatedKey("subjectNo")
  String subjectNo;

  Integer contraindicationBundle = 1;

  @JsonPopulatedKey("dropoutDate")
  String dropoutDate;

  @JsonPopulatedKey("completeDate")
  String completeDate;

  @ManyToOne(cascade = CascadeType.MERGE)
  @JoinTable(name = "case_subject",
      joinColumns = { @JoinColumn(name = "subject_id") },
      inverseJoinColumns = { @JoinColumn(name = "case_id") })
  CaseStudy caseStudy;

  @DiffIgnore
  @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL,
      orphanRemoval = true, fetch = FetchType.EAGER)
  List<Visit> visits = new ArrayList<>();

  public Subject() {}

  public Subject(JsonNode jsonNode) {
    setFormData(jsonNode);
  }

  public long unreviewedVisits() {
    return visits.stream().filter(v -> !v.isReviewed()).count();
  }

  @Override
  public void setFormData(JsonNode formData) {
    this.formData = formData;
    String json = "{}";
    try {
      json = FlattenedJsonTypeConfigurer.INSTANCE.getObjectMapperFactory().get()
          .writeValueAsString(formData);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    setPopulatedJson(json);
  }

  @Override
  public JsonNode getSchema() {
    return JsonSchema.SUBJECT_SCHEMA;
  }

  @Override
  public JsonNode getUiSchema() {
    return JsonSchema.SUBJECT_UI_SCHEMA;
  }

  @Convert(converter = JsonNodeConverter.class)
  @Column(columnDefinition = "text")
  protected JsonNode formData = FlattenedJsonTypeConfigurer.INSTANCE
      .getObjectMapperFactory().get().createObjectNode();

  public enum Status {

    PRESCREENING, SCREENING, UNQUALIFIED, ONGOING, DROPPED, FOLLOWUP, CLOSED;

    public static Status fromString(String status) {
      switch (status.toUpperCase()) {
        case "PRESCREENING":
          return PRESCREENING;
        case "SCREENING":
          return SCREENING;
        case "UNQUALIFIED":
          return UNQUALIFIED;
        case "ONGOING":
          return ONGOING;
        case "DROPPED":
          return DROPPED;
        case "FOLLOWUP":
          return FOLLOWUP;
        case "CLOSED":
          return CLOSED;
        default:
          return PRESCREENING;
      }
    }

  }

}
