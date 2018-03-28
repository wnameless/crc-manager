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
package com.wmw.crc.manager;

import static com.google.common.base.Charsets.UTF_8;

import java.io.IOException;
import java.net.URL;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.io.Resources;
import com.wmw.crc.manager.model.CRC;
import com.wmw.crc.manager.model.Case;
import com.wmw.crc.manager.model.CaseSupplement1;
import com.wmw.crc.manager.model.CaseSupplement2;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.repository.CRCRepository;
import com.wmw.crc.manager.repository.CaseRepository;
import com.wmw.crc.manager.repository.CaseSupplement1Repository;
import com.wmw.crc.manager.repository.CaseSupplement2Repository;
import com.wmw.crc.manager.repository.SubjectRepository;

import main.java.com.maximeroussy.invitrode.RandomWord;
import main.java.com.maximeroussy.invitrode.WordLengthException;
import net.sf.rubycollect4j.Ruby;

@Component
public class DatabaseInitializer {

  @Autowired
  CaseRepository caseRepo;

  @Autowired
  CaseSupplement1Repository caseSupp1Repo;

  @Autowired
  CaseSupplement2Repository caseSupp2Repo;

  @Autowired
  CRCRepository crcRepo;

  @Autowired
  SubjectRepository subjectRepo;

  @PostConstruct
  void init() throws IOException, WordLengthException {
    int i = 0;
    while (caseRepo.count() < 10) {
      Case c = new Case();
      caseRepo.save(c);

      switch (i % 4) {
        case 0:
          c.setStatus(Case.Status.NEW);
          break;
        case 1:
          c.setStatus(Case.Status.EXEC);
          break;
        case 2:
          c.setStatus(Case.Status.END);
          break;
        case 3:
          c.setStatus(Case.Status.NONE);
          break;
      }

      c.setYear(2019);
      c.setPiName(RandomWord.getNewWord(4) + " " + RandomWord.getNewWord(6));
      c.setCaseNumber(Integer.toString(12425 + i));
      c.setTrialName(RandomWord.getNewWord(5));
      c.setTrialNameEng(RandomWord.getNewWord(5));
      c.setCoPiName(RandomWord.getNewWord(5) + " " + RandomWord.getNewWord(5));
      c.setAssociatePiName(
          RandomWord.getNewWord(6) + " " + RandomWord.getNewWord(4));
      c.setProjectNumber(RandomWord.getNewWord(6));
      c.setProjectType(RandomWord.getNewWord(4));
      c.setExpectedNumberOfSubjectsLocal(100);
      c.setExpectedNumberOfSubjectsNational(1000);
      c.setExpectedNumberOfSubjectsGlobal(10000);
      c.setExpectedStartDate(Ruby.Date.today());
      c.setExpectedEndDate(Ruby.Date.today().add(300).days());
      URL url = Resources.getResource("json-schema/新進案件區-part1-formData.json");
      c.setJsonData(Resources.toString(url, UTF_8));

      CaseSupplement1 cs1 = new CaseSupplement1();
      url = Resources.getResource("json-schema/新進案件區-part3-formData.json");
      cs1.setJsonData(Resources.toString(url, UTF_8));
      caseSupp1Repo.save(cs1);
      c.setSupplement1(cs1);

      CaseSupplement2 cs2 = new CaseSupplement2();
      url = Resources.getResource("json-schema/新進案件區-part3-formData.json");
      cs2.setJsonData(Resources.toString(url, UTF_8));
      caseSupp2Repo.save(cs2);
      c.setSupplement2(cs2);

      CRC crc = new CRC();
      url = Resources.getResource("json-schema/執行案件區-禁忌用藥專區-formData.json");
      crc.setJsonData(Resources.toString(url, UTF_8));
      crcRepo.save(crc);
      c.setCrc(crc);

      Subject subject = new Subject();
      url = Resources.getResource("json-schema/執行案件區-新增受試者(單筆)-formData.json");
      subject.setJsonData(Resources.toString(url, UTF_8));
      subjectRepo.save(subject);
      c.getSubjects().add(subject);

      caseRepo.save(c);

      i++;
    }
  }

}
