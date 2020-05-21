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
package com.wmw.crc.manager.util;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.joda.time.DateTime;
import org.springframework.web.multipart.MultipartFile;

import com.github.wnameless.jpa.type.flattenedjson.FlattenedJsonTypeConfigurer;
import com.github.wnameless.workbookaccessor.WorkbookReader;
import com.google.common.base.Strings;
import com.wmw.crc.manager.model.Subject;

import net.sf.rubycollect4j.Ruby;

public class TsghExcelSubjects implements ExcelSubjects {

  private static final List<String> titles =
      Ruby.Array.of("醫院病歷號", "姓名", "試驗病歷號", "篩選號碼", "受試號碼", "生日", "電話號碼", "ID",
          "地址", "簽ICF日期", "體檢日期", "嚴重不良事件數").freeze();

  private Workbook wb;
  private List<Subject> subjects = newArrayList();

  private String errorMessage;

  public TsghExcelSubjects(MultipartFile file) {
    try {
      wb = WorkbookFactory.create(file.getInputStream());
      extractSubjects();
    } catch (EncryptedDocumentException | IOException e) {
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

        if (!isValidDate(row.get("生日")) || !isValidDate(row.get("簽ICF日期"))
            || !isValidDate(row.get("體檢日期"))) {
          errorMessage = "存在不符合格式日期";
          return;
        }
        if (!isValidInteger(row.get("嚴重不良事件數"))) {
          errorMessage = "存在不符合格式數字";
          return;
        }

        Map<String, Object> initData = new HashMap<>();
        initData.put("mrn", row.get("醫院病歷號"));
        initData.put("lastname", row.get("姓名"));
        initData.put("subjectId", row.get("試驗病歷號"));
        initData.put("screenNo", row.get("篩選號碼"));
        initData.put("subjectNo", row.get("受試號碼"));
        if (row.get("生日") != null && !row.get("生日").isEmpty()) {
          initData.put("birthDate", normalizeDate(row.get("生日")));
        }
        if (!Strings.isNullOrEmpty(row.get("電話號碼"))) {
          initData.put("telephone1", row.get("電話號碼"));
        }
        initData.put("taiwanId", row.get("ID"));
        initData.put("address", row.get("地址"));
        if (row.get("簽ICF日期") != null && !row.get("簽ICF日期").isEmpty()) {
          initData.put("icfDate", normalizeDate(row.get("簽ICF日期")));
        }
        if (row.get("體檢日期") != null && !row.get("體檢日期").isEmpty()) {
          initData.put("examDate", normalizeDate(row.get("體檢日期")));
        }
        initData.put("saeCount", normalizeInteger(row.get("嚴重不良事件數")));

        Subject subject = new Subject();
        subject.setFormData(FlattenedJsonTypeConfigurer.INSTANCE
            .getObjectMapperFactory().get().valueToTree(initData));

        if (subject.getNationalId() != null
            && !subject.getNationalId().trim().isEmpty()) {
          list.add(subject);
        }
      }

      subjects.addAll(list);
    } else {
      List<String> missingHeader = wr.getHeader();
      missingHeader.removeAll(titles);
      errorMessage = "Excel表頭缺失: " + missingHeader;
    }
  }

  private int normalizeInteger(String intStr) {
    if (intStr == null || intStr.trim().isEmpty()) return 0;
    int i = 0;
    try {
      i = Integer.parseInt(intStr);
    } catch (NumberFormatException e) {}
    return i < 0 ? 0 : i;
  }

  private String normalizeDate(String dateStr) {
    if (dateStr == null || dateStr.isEmpty()) return dateStr;

    Date date = null;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    try {
      date = sdf.parse(dateStr);
      if (!dateStr.equals(sdf.format(date))) {
        date = null;
      }
    } catch (ParseException e) {
      DateTime dt = new DateTime().withDate(1899, 12, 31)
          .plusDays(Integer.parseInt(dateStr) - 1);
      date = dt.toDate();
    }

    sdf = new SimpleDateFormat("yyyy-MM-dd");
    return sdf.format(date);
  }

  private boolean isValidInteger(String value) {
    if (value == null || value.isEmpty()) return true;

    try {
      Integer.parseInt(value);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  private boolean isValidDate(String value) {
    if (value == null || value.isEmpty() || value.matches("\\d+")) return true;

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

  @Override
  public String getErrorMessage() {
    return errorMessage;
  }

  @Override
  public List<Subject> getSubjects() {
    return subjects;
  }

}
