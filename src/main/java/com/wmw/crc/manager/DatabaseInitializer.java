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
public class DatabaseInitializer {

  @Autowired
  CaseRepository caseRepo;

  @Autowired
  SubjectRepository subjectRepo;

  @Autowired
  MedicineRepository medicineRepo;

  @PostConstruct
  void init() throws IOException, WordLengthException {
    if (caseRepo.count() < 10) {
      Case c = new Case();
      URL url = Resources.getResource("json-schemas/test-data/4.json");
      BaseJsonSchemaForm bjsf = new Gson().fromJson(
          Resources.toString(url, Charsets.UTF_8), BaseJsonSchemaForm.class);
      c.setJsonSchema(bjsf.getJsonSchema());
      c.setJsonData(bjsf.getJsonData());
      c.setJsonUiSchema(bjsf.getJsonUiSchema());
      caseRepo.save(c);

      c = new Case();
      url = Resources.getResource("json-schemas/test-data/5.json");
      bjsf = new Gson().fromJson(Resources.toString(url, Charsets.UTF_8),
          BaseJsonSchemaForm.class);
      c.setJsonSchema(bjsf.getJsonSchema());
      c.setJsonData(bjsf.getJsonData());
      c.setJsonUiSchema(bjsf.getJsonUiSchema());
      caseRepo.save(c);

      c = new Case();
      url = Resources.getResource("json-schemas/test-data/6.json");
      bjsf = new Gson().fromJson(Resources.toString(url, Charsets.UTF_8),
          BaseJsonSchemaForm.class);
      c.setJsonSchema(bjsf.getJsonSchema());
      c.setJsonData(bjsf.getJsonData());
      c.setJsonUiSchema(bjsf.getJsonUiSchema());
      caseRepo.save(c);
    }

    // int i = 0;
    // while (caseRepo.count() < 10) {
    // Case c = new Case();
    // caseRepo.save(c);
    //
    // switch (i % 4) {
    // case 0:
    // c.setStatus(Case.Status.NEW);
    // break;
    // case 1:
    // c.setStatus(Case.Status.EXEC);
    // break;
    // case 2:
    // c.setStatus(Case.Status.END);
    // break;
    // case 3:
    // c.setStatus(Case.Status.NONE);
    // break;
    // }
    //
    // c.setPiName(RandomWord.getNewWord(4) + " " + RandomWord.getNewWord(6));
    // c.setCaseNumber(Integer.toString(12425 + i));
    // c.setTrialName(RandomWord.getNewWord(5));
    // c.setTrialNameEng(RandomWord.getNewWord(5));
    // c.setCoPiName(RandomWord.getNewWord(5) + " " + RandomWord.getNewWord(5));
    // c.setAssociatePiName(
    // RandomWord.getNewWord(6) + " " + RandomWord.getNewWord(4));
    // c.setProjectNumber(RandomWord.getNewWord(6));
    // c.setProjectType(RandomWord.getNewWord(4));
    // c.setExpectedNumberOfSubjectsLocal(100);
    // c.setExpectedNumberOfSubjectsNational(1000);
    // c.setExpectedNumberOfSubjectsGlobal(10000);
    // c.setExpectedStartDate("2017/11/12");
    // c.setExpectedEndDate("2018/7/8");
    // URL url = Resources.getResource(JsonSchemaPath.applicationData);
    // c.setJsonData(Resources.toString(url, UTF_8));
    //
    // Subject subject = new Subject();
    // url = Resources.getResource(JsonSchemaPath.subjectData);
    // subject.setJsonData(Resources.toString(url, UTF_8));
    // subjectRepo.save(subject);
    // c.getSubjects().add(subject);
    // if (i != 0) c.setOwner("pi07");
    //
    // caseRepo.save(c);
    //
    // i++;
    // }

  }

}
