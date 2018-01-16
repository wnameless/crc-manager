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
package com.wmw.crc.manager.form.json.schema;

import static com.google.common.base.Charsets.UTF_8;

import java.io.IOException;
import java.net.URL;

import com.google.common.io.Resources;

public class TriServiceFormJsonSchemaProvider
    implements FormJsonSchemaProvider {

  @Override
  public String getFormSchema() {
    URL url = Resources.getResource("json-schema/form-schema.json");
    String schema = null;
    try {
      schema = Resources.toString(url, UTF_8);
    } catch (IOException e) {}

    return schema;
  }

  @Override
  public String getFormUiSchema() {
    URL url = Resources.getResource("json-schema/form-ui-schema.json");
    String schema = null;
    try {
      schema = Resources.toString(url, UTF_8);
    } catch (IOException e) {}

    return schema;
  }

  @Override
  public String getFormSupplement1Schema() {
    URL url = Resources.getResource("json-schema/form-supplement1-schema.json");
    String schema = null;
    try {
      schema = Resources.toString(url, UTF_8);
    } catch (IOException e) {}

    return schema;
  }

  @Override
  public String getFormSupplement1UiSchema() {
    URL url = Resources.getResource("json-schema/form-supplement1-schema.json");
    String schema = null;
    try {
      schema = Resources.toString(url, UTF_8);
    } catch (IOException e) {}

    return schema;
  }

  @Override
  public String getFormSupplement2Schema() {
    URL url = Resources.getResource("json-schema/form-supplement2-schema.json");
    String schema = null;
    try {
      schema = Resources.toString(url, UTF_8);
    } catch (IOException e) {}

    return schema;
  }

  @Override
  public String getFormSupplement2UiSchema() {
    URL url =
        Resources.getResource("json-schema/form-supplement2-ui-schema.json");
    String schema = null;
    try {
      schema = Resources.toString(url, UTF_8);
    } catch (IOException e) {}

    return schema;
  }

}
