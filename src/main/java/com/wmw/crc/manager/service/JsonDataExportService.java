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
package com.wmw.crc.manager.service;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.github.wnameless.workbookaccessor.WorkbookWriter;
import com.wmw.crc.manager.model.Case;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.util.MinimalJsonUtils;

import net.sf.rubycollect4j.Ruby;

@Service
public class JsonDataExportService {

  public Workbook toExcel(Case kase) {
    JsonObject jsonSchema = Json.parse(kase.getJsonSchema()).asObject();
    JsonObject jsonData = Json.parse(kase.getJsonData()).asObject();

    WorkbookWriter ww = WorkbookWriter.openXLSX();

    ww.setSheetName("案件內容");
    JsonObject properties = jsonSchema.get("properties").asObject();
    for (String key : properties.names()) {
      if (key.equals("requiredFiles") || key.equals("preview")) continue;

      List<Object> row = newArrayList();
      row.add(properties.get(key).asObject().get("title") == null ? key
          : properties.get(key).asObject().get("title").asString());
      if (jsonData.names().contains(key)) {
        row.addAll(MinimalJsonUtils.splitValToList(jsonData.get(key)));
      } else {
        row.add("");
      }
      ww.addRow(row);
    }

    ww.createAndTurnToSheet("受試者");
    JsonObject schema = Json.parse(new Subject().getJsonSchema()).asObject();
    JsonObject props = schema.get("properties").asObject();
    ww.addRow(Ruby.Array.of(props.names())
        .map(key -> props.get(key).asObject().get("title").asString()));
    for (Subject subject : kase.getSubjects()) {
      JsonObject data = Json.parse(subject.getJsonData()).asObject();
      ww.addRow(
          Ruby.Array.of(props.names()).map(key -> data.names().contains(key)
              ? MinimalJsonUtils.val2String(data.get(key)) : ""));
    }

    return ww.getWorkbook();
  }

}
