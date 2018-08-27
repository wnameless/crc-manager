package com.wmw.crc.manager.util;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.github.wnameless.json.JsonInitValueCustomizer;
import com.wmw.crc.manager.model.Subject;

public class SubjectStatusCustomizer implements JsonInitValueCustomizer {

  @Override
  public Object toValue(String jsonData) {
    JsonValue jv = Json.parse(jsonData);
    JsonObject jo = jv.asObject();

    String[] icf = jo.getString("icfDate", "").split("-");
    String[] exam = jo.getString("examDate", "").split("-");
    String[] accrual = jo.getString("accrualDate", "").split("-");
    String[] complete = jo.getString("completeDate", "").split("-");
    String[] dropout = jo.getString("dropoutDate", "").split("-");

    if (dropout.length >= 3) {
      return Subject.Status.DROPPED;
    }

    if (complete.length >= 3) {
      return Subject.Status.CLOSED;
    }

    if (accrual.length >= 3) {
      return Subject.Status.ONGOING;
    }

    if (exam.length >= 3) {
      return Subject.Status.SCREENING;
    }

    if (icf.length >= 3) {
      return Subject.Status.PRESCREENING;
    }

    return Subject.Status.PRESCREENING;
  }

}
