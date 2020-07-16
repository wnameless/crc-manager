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
package com.wmw.crc.manager;

import static com.google.common.base.Charsets.UTF_8;

import java.io.IOException;
import java.net.URL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

public final class JsonSchema {

  private static final String applicationSchema =
      "json-schemas/application-schema.json";
  private static final String applicationUISchema =
      "json-schemas/application-ui-schema.json";

  private static final String subjectSchema = //
      "json-schemas/subject-schema.json";
  private static final String subjectUISchema =
      "json-schemas/subject-ui-schema.json";

  private static final String emailsSchema = //
      "json-schemas/emails-schema.json";

  private static final String bundleDescriptionSchema = //
      "json-schemas/bundle-description-schema.json";
  private static final String bundleDescriptionUISchema =
      "json-schemas/bundle-description-ui-schema.json";

  public static final JsonNode EMPTY_SCHEMA;
  public static final JsonNode APPLICATION_SCHEMA;
  public static final JsonNode APPLICATION_UI_SCHEMA;
  public static final JsonNode SUBJECT_SCHEMA;
  public static final JsonNode SUBJECT_UI_SCHEMA;
  public static final JsonNode EMAIL_SCHEMA;
  public static final JsonNode BUNDLE_DESCRIPTION_SCHEMA;
  public static final JsonNode BUNDLE_DESCRIPTION_UI_SCHEMA;
  static {
    URL url = Resources.getResource(JsonSchema.applicationSchema);
    JsonNode jsonNode = null;
    try {
      jsonNode = new ObjectMapper().readTree(Resources.toString(url, UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
    APPLICATION_SCHEMA = jsonNode;

    url = Resources.getResource(JsonSchema.applicationUISchema);
    try {
      jsonNode = new ObjectMapper().readTree(Resources.toString(url, UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
    APPLICATION_UI_SCHEMA = jsonNode;

    url = Resources.getResource(JsonSchema.subjectSchema);
    try {
      jsonNode = new ObjectMapper().readTree(Resources.toString(url, UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
    SUBJECT_SCHEMA = jsonNode;

    url = Resources.getResource(JsonSchema.subjectUISchema);
    try {
      jsonNode = new ObjectMapper().readTree(Resources.toString(url, UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
    SUBJECT_UI_SCHEMA = jsonNode;

    url = Resources.getResource(JsonSchema.emailsSchema);
    try {
      jsonNode = new ObjectMapper().readTree(Resources.toString(url, UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
    EMAIL_SCHEMA = jsonNode;

    url = Resources.getResource(JsonSchema.bundleDescriptionSchema);
    try {
      jsonNode = new ObjectMapper().readTree(Resources.toString(url, UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
    BUNDLE_DESCRIPTION_SCHEMA = jsonNode;

    url = Resources.getResource(JsonSchema.bundleDescriptionUISchema);
    try {
      jsonNode = new ObjectMapper().readTree(Resources.toString(url, UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
    BUNDLE_DESCRIPTION_UI_SCHEMA = jsonNode;

    try {
      jsonNode = new ObjectMapper().readTree("{}");
    } catch (IOException e) {
      e.printStackTrace();
    }
    EMPTY_SCHEMA = jsonNode;
  }

}
