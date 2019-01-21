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

import java.io.IOException;
import java.net.URL;
import javax.annotation.PostConstruct;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.wmw.crc.manager.model.Medicine;
import com.wmw.crc.manager.repository.MedicineRepository;

@Profile("test")
@Component
public class MedicinesInitializer {

  @Autowired
  MedicineRepository medicineRepo;

  @PostConstruct
  void init() throws IOException {
    if (medicineRepo.count() < 10) {
      medicineRepo.deleteAll();

      URL medicines = Resources.getResource("medicines.json");
      JsonValue medJsonArray =
          Json.parse(IOUtils.toString(medicines, Charsets.UTF_8));

      for (JsonValue med : medJsonArray.asArray()) {
        Medicine m = new Medicine();
        m.setName(med.asObject().getString("name", null));
        m.setEngName(med.asObject().getString("engName", null));
        JsonArray atc = med.asObject().get("atcCode").asArray();
        for (int idx = 0; idx < atc.size(); idx++) {
          switch (idx) {
            case 0:
              m.setAtcCode1(atc.get(idx).asString());
              break;
            case 1:
              m.setAtcCode2(atc.get(idx).asString());
              break;
            case 2:
              m.setAtcCode3(atc.get(idx).asString());
              break;
            case 3:
              m.setAtcCode4(atc.get(idx).asString());
              break;
          }
          medicineRepo.save(m);
        }
      }
    }
  }

}
