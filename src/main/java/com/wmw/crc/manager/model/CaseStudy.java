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
import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashSet;
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
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.apache.commons.lang3.EnumUtils;
import org.javers.core.metamodel.annotation.DiffIgnore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.jpa.type.flattenedjson.FlattenedJsonTypeConfigurer;
import com.github.wnameless.jpa.type.flattenedjson.JsonNodeConverter;
import com.github.wnameless.json.beanpopulator.JsonPopulatable;
import com.github.wnameless.json.beanpopulator.JsonPopulatedKey;
import com.github.wnameless.spring.common.web.RestfulItem;
import com.github.wnameless.spring.react.jsf.ReactJsonSchemaForm;
import com.google.common.io.Resources;
import com.wmw.crc.manager.JsonSchemaPath;
import com.wmw.crc.manager.RestfulPath;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.sf.rubycollect4j.Ruby;

@EqualsAndHashCode(callSuper = false, of = { "id" })
@Data
@Table(indexes = { //
    @Index(columnList = "irbNumber", unique = false),
    @Index(columnList = "trialName", unique = false),
    @Index(columnList = "piName", unique = false),
    @Index(columnList = "expectedStartDate", unique = false),
    @Index(columnList = "expectedEndDate", unique = false) })
@Entity
public class CaseStudy
    implements JsonPopulatable, ReactJsonSchemaForm, RestfulItem<Long> {

  @Override
  public String getIndexPath() {
    return "/" + RestfulPath.Names.CASE_STUDY;
  }

  @Id
  @GeneratedValue
  Long id;

  @JsonPopulatedKey("irbNum")
  String irbNumber;

  @JsonPopulatedKey("adminNum")
  String adminNumber;

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

  @JsonPopulatedKey("contractStartDate")
  String expectedStartDate;

  @JsonPopulatedKey("contractEndDate")
  String expectedEndDate;

  @Enumerated(EnumType.STRING)
  Status status = Status.NEW;

  @DiffIgnore
  @OneToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "case_subject", joinColumns = @JoinColumn(name = "case_id"),
      inverseJoinColumns = @JoinColumn(name = "subject_id"))
  List<Subject> subjects = newArrayList();

  Long unreviewedOngoingVisits = 0L;

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
  Set<String> managers = new LinkedHashSet<>();

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "case_editors",
      joinColumns = @JoinColumn(name = "case_id"))
  Set<String> editors = new LinkedHashSet<>();

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "case_viewers",
      joinColumns = @JoinColumn(name = "case_id"))
  Set<String> viewers = new LinkedHashSet<>();

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "case_emails",
      joinColumns = @JoinColumn(name = "case_id"))
  Set<String> emails = new LinkedHashSet<>();

  @OrderColumn
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "case_bundledesciptions",
      joinColumns = @JoinColumn(name = "case_id"))
  List<String> bundleDesciption = Ruby.Range.of("1", "9").toList();

  public CaseStudy() {}

  public List<String> getBundleLabels() {
    return bundleDesciption.isEmpty() ? Ruby.Range.of("1", "9").toList()
        : bundleDesciption;
  }

  public String bundleLabel(int bundle) {
    return getBundleLabels().get(bundle - 1);
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

  public enum Status {
    NEW, EXEC, END, NONE;

    public static Status fromString(String status) {
      return EnumUtils.getEnumIgnoreCase(Status.class, status);
    }

    public static Status fromString(String status, Status defaultVal) {
      return firstNonNull(fromString(status), defaultVal);
    }
  }

}
