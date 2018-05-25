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

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.wmw.crc.manager.model.Case;

@Service
public class JsonDataExportService {

  public void toExcel(Case kase) {
    JsonObject jsonSchema = Json.parse(kase.getJsonSchema()).asObject();
    JsonObject jsonData = Json.parse(kase.getJsonData()).asObject();

    XSSFWorkbook workbook = new XSSFWorkbook();

  }

}
