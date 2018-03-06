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
import com.github.wnameless.spring.json.schema.form.JpaJsonSchemaForm;
import com.google.common.io.Resources;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false, of = { "id" })
@Data
@Entity
public class CaseSupplement2 extends JpaJsonSchemaForm
    implements JsonDataInitailizable {

  @Id
  @GeneratedValue
  Long id;

  boolean formDone;

  public CaseSupplement2() {
    try {
      URL url =
          Resources.getResource("json-schema/新進案件區-part3-JSONSchema.json");
      setJsonSchema(Resources.toString(url, UTF_8));
      url = Resources.getResource("json-schema/新進案件區-part3-UISchema.json");
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
