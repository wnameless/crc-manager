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

import java.io.IOException;
import java.net.URL;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.github.wnameless.json.JsonDataInitailizable;
import com.github.wnameless.json.JsonInitKey;
import com.github.wnameless.spring.json.schema.form.JpaJsonSchemaForm;
import com.google.common.io.Resources;
import com.wmw.crc.manager.JsonSchemaPath;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false, of = { "id" })
@Data
@Entity
public class Subject extends JpaJsonSchemaForm
    implements JsonDataInitailizable {

  public enum Status {

    PRESCREENING("預篩"), SCREENING("篩查"), UNQUALIFIED("失格"), ONGOING("執行"),
    DROPPED("排除"), FOLLOWUP("追蹤"), CLOSED("結案");

    private String literal;

    private Status(String literal) {
      this.literal = literal;
    }

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

    @Override
    public String toString() {
      return literal;
    }

  }

  @Id
  @GeneratedValue
  Long id;

  Status status = Status.PRESCREENING;

  @JsonInitKey("lastname")
  String name;

  @JsonInitKey("taiwanId")
  String nationalId;

  public Subject() {
    try {
      URL url = Resources.getResource(JsonSchemaPath.subjectSchema);
      String json = Resources.toString(url, UTF_8);
      setJsonSchema(json);

      url = Resources.getResource(JsonSchemaPath.subjectUISchema);
      json = Resources.toString(url, UTF_8);
      setJsonUiSchema(json);
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
