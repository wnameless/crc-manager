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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.wmw.crc.manager.model.Case;
import com.wmw.crc.manager.model.form.BaseJsonSchemaForm;
import com.wmw.crc.manager.repository.CaseRepository;
import com.wmw.crc.manager.repository.MedicineRepository;
import com.wmw.crc.manager.repository.SubjectRepository;

import main.java.com.maximeroussy.invitrode.WordLengthException;

@Profile("test")
@Component
public class TestDatabaseInitializer {

  @Autowired
  CaseRepository caseRepo;

  @Autowired
  SubjectRepository subjectRepo;

  @Autowired
  MedicineRepository medicineRepo;

  @PostConstruct
  void init() throws IOException, WordLengthException {
    if (caseRepo.count() == 0) {
      Case c = new Case();
      URL url = Resources.getResource("json-schemas/test-data/4.json");
      BaseJsonSchemaForm bjsf = new Gson().fromJson(
          Resources.toString(url, Charsets.UTF_8), BaseJsonSchemaForm.class);
      c.setFormData(bjsf.getFormData());
      caseRepo.save(c);

      c = new Case();
      url = Resources.getResource("json-schemas/test-data/5.json");
      bjsf = new Gson().fromJson(Resources.toString(url, Charsets.UTF_8),
          BaseJsonSchemaForm.class);
      c.setFormData(bjsf.getFormData());
      caseRepo.save(c);

      c = new Case();
      url = Resources.getResource("json-schemas/test-data/6.json");
      bjsf = new Gson().fromJson(Resources.toString(url, Charsets.UTF_8),
          BaseJsonSchemaForm.class);
      c.setFormData(bjsf.getFormData());
      caseRepo.save(c);
    }

  }

}
