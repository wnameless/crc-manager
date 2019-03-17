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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.repository.MedicineRepository;
import com.wmw.crc.manager.repository.SubjectRepository;

@Profile("test")
@Component
public class TestDatabaseInitializer {

  @Autowired
  CaseStudyRepository caseRepo;

  @Autowired
  SubjectRepository subjectRepo;

  @Autowired
  MedicineRepository medicineRepo;

  @PostConstruct
  void init() throws IOException {
    if (caseRepo.count() == 0) {
      CaseStudy c = new CaseStudy();
      URL url = Resources.getResource("json-schemas/test-data/4.json");
      JsonNode bjsf =
          new ObjectMapper().readTree(Resources.toString(url, Charsets.UTF_8));
      c.setFormData(bjsf.get("formData"));
      caseRepo.save(c);

      c = new CaseStudy();
      url = Resources.getResource("json-schemas/test-data/5.json");
      bjsf =
          new ObjectMapper().readTree(Resources.toString(url, Charsets.UTF_8));
      c.setFormData(bjsf.get("formData"));
      caseRepo.save(c);

      c = new CaseStudy();
      url = Resources.getResource("json-schemas/test-data/6.json");
      bjsf =
          new ObjectMapper().readTree(Resources.toString(url, Charsets.UTF_8));
      c.setFormData(bjsf.get("formData"));
      caseRepo.save(c);
    }

  }

}
