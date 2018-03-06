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
package com.github.wnameless.spring.json.schema.form;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class JpaJsonSchemaForm implements JsonSchemaForm {

  @Column
  @Lob
  protected String jsonData;

  @Column
  @Lob
  protected String jsonSchema;

  @Column
  @Lob
  protected String jsonUiSchema;

  @Override
  public String getJsonData() {
    return jsonData;
  }

  @Override
  public void setJsonData(String jsonData) {
    this.jsonData = jsonData;
  }

  @Override
  public String getJsonSchema() {
    return jsonSchema;
  }

  @Override
  public void setJsonSchema(String jsonSchema) {
    this.jsonSchema = jsonSchema;
  }

  @Override
  public String getJsonUiSchema() {
    return jsonUiSchema;
  }

  @Override
  public void setJsonUiSchema(String jsonUiSchema) {
    this.jsonUiSchema = jsonUiSchema;
  }

}
