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
package com.wmw.crc.manager.service;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.wnameless.workbookaccessor.WorkbookWriter;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.repository.SubjectRepository;
import com.wmw.crc.manager.util.JsonNodeUtils;

import net.sf.rubycollect4j.Ruby;
import net.sf.rubycollect4j.RubyArray;

@Service
public class JsonDataExportService {

  @Autowired
  MessageSource messageSource;

  @Autowired
  SubjectRepository subjectRepo;

  public Workbook toExcel(CaseStudy kase, Locale locale) {
    JsonNode jsonSchema = kase.getSchema();
    JsonNode jsonData = kase.getFormData();

    WorkbookWriter ww = WorkbookWriter.openXLSX();

    ww.setSheetName(messageSource.getMessage("service.export.sheet.case.name",
        new Object[] {}, locale));
    JsonNode properties = jsonSchema.get("properties");
    Iterator<Entry<String, JsonNode>> fields = properties.fields();
    while (fields.hasNext()) {
      Entry<String, JsonNode> f = fields.next();
      if (f.getKey().equals("requiredFiles") || f.getKey().equals("preview"))
        continue;

      List<Object> row = newArrayList();
      row.add(f.getValue().get("title") == null ? f.getKey()
          : f.getValue().get("title").textValue());
      if (jsonData.has(f.getKey())) {
        row.addAll(JsonNodeUtils.splitValToList(jsonData.get(f.getKey())));
      } else {
        row.add("");
      }
      ww.addRow(row);
    }

    ww.createAndTurnToSheet(messageSource.getMessage(
        "service.export.sheet.subject.name", new Object[] {}, locale));
    JsonNode schema = new Subject().getSchema();
    JsonNode props = schema.get("properties");
    RubyArray<String> fieldNames = Ruby.Array.copyOf(props.fieldNames());
    ww.addRow(fieldNames.map(key -> props.get(key).get("title").textValue()));
    for (Subject subject : subjectRepo.findAllByCaseStudy(kase)) {
      JsonNode data = subject.getFormData();
      ww.addRow(fieldNames.map(
          key -> data.has(key) ? JsonNodeUtils.val2String(data.get(key)) : ""));
    }

    return ww.getWorkbook();
  }

}
