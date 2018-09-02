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
package com.wmw.crc.manager.util;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.multipart.MultipartFile;

import com.github.wnameless.workbookaccessor.WorkbookReader;
import com.google.gson.Gson;
import com.wmw.crc.manager.model.Subject;

import net.sf.rubycollect4j.Ruby;

public class ExcelSubjects {

  private static final List<String> titles = Ruby.Array
      .of("一般病例號", "姓名", "病歷號碼", "生日", "電話號碼", "ID", "地址", "簽ICF日期", "體檢日期")
      .freeze();

  private Workbook wb;
  private List<Subject> subjects = newArrayList();

  private String errorMessage;

  public ExcelSubjects(MultipartFile file) {
    try {
      wb = WorkbookFactory.create(file.getInputStream());
      extractSubjects();
    } catch (EncryptedDocumentException | InvalidFormatException
        | IOException e) {
      errorMessage = "檔案不支援";
    }
  }

  private void extractSubjects() {
    List<Subject> list = newArrayList();

    WorkbookReader wr = WorkbookReader.open(wb);
    if (wr.getHeader().containsAll(titles)) {
      for (Map<String, String> row : wr.toMaps()) {
        row = Ruby.Hash.copyOf(row).deleteIf((k, v) -> {
          return !titles.contains(k);
        }).toMap();

        if (!isValidFormat(row.get("生日")) || !isValidFormat(row.get("簽ICF日期"))
            || !isValidFormat(row.get("體檢日期"))) {
          errorMessage = "存在不符合格式日期";
          return;
        }

        Map<String, String> initData = newHashMap();
        initData.put("mrn", row.get("一般病例號"));
        initData.put("lastname", row.get("姓名"));
        initData.put("subjectId", row.get("病歷號碼"));
        if (row.get("生日") != null && !row.get("生日").isEmpty()) {
          initData.put("birthDate", normalizeDate(row.get("生日")));
        }
        initData.put("telephone1", row.get("電話號碼"));
        initData.put("taiwanId", row.get("ID"));
        initData.put("address", row.get("地址"));
        if (row.get("簽ICF日期") != null && !row.get("簽ICF日期").isEmpty()) {
          initData.put("icfDate", normalizeDate(row.get("簽ICF日期")));
        }
        if (row.get("體檢日期") != null && !row.get("體檢日期").isEmpty()) {
          initData.put("examDate", normalizeDate(row.get("體檢日期")));
        }

        Subject subject = new Subject();
        subject.setJsonData(new Gson().toJson(initData));

        list.add(subject);
      }

      subjects.addAll(list);
    } else {
      errorMessage = "Excel表頭不齊全";
    }
  }

  private String normalizeDate(String date) {
    if (date == null || date.isEmpty()) return date;
    return date.replace('/', '-');
  }

  private boolean isValidFormat(String value) {
    if (value == null || value.isEmpty()) return true;

    Date date = null;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    try {
      date = sdf.parse(value);
      if (!value.equals(sdf.format(date))) {
        date = null;
      }
    } catch (ParseException e) {}

    return date != null;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public List<Subject> getSubjects() {
    return subjects;
  }

}
