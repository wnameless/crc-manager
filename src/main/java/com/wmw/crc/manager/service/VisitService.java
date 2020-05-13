/*
 *
 * Copyright 2020 Wei-Ming Wu
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.wmw.crc.manager.controller.api.NewVisit;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.CaseStudy.Status;
import com.wmw.crc.manager.model.Contraindication;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.model.Visit;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.repository.ContraindicationRepository;
import com.wmw.crc.manager.repository.SubjectRepository;
import com.wmw.crc.manager.repository.VisitRepository;

import lombok.extern.slf4j.Slf4j;
import net.sf.rubycollect4j.Ruby;

@Slf4j
@Service
public class VisitService {

  @Autowired
  Environment env;

  @Autowired
  CrcManagerService crcManagerService;

  @Autowired
  CaseStudyRepository caseStudyRepo;

  @Autowired
  SubjectRepository subjectRepo;

  @Autowired
  VisitRepository visitRepo;

  @Autowired
  ContraindicationRepository contraindicationRepo;

  @Autowired
  JavaMailSender emailSender;

  // @Value("${contraindication.manager.mail}")
  // String contraindicationManagerMail;

  public void addVisit(NewVisit newVisit) {
    List<Subject> subjects =
        crcManagerService.findExecSubjects(newVisit.getNationalId()).get();

    for (Subject s : subjects) {
      Visit visit = new Visit();
      if (newVisit.isContraindicationSuspected()) {
        BeanUtils.copyProperties(newVisit, visit);
        visit.setSubject(s);
        visitRepo.save(visit);
      } else {
        boolean isExisted = visitRepo
            .existsBySubjectAndDivisionAndDoctorAndRoomAndDateAndContraindicationSuspected(
                s, newVisit.getDivision(), newVisit.getDoctor(),
                newVisit.getRoom(), newVisit.getDate(), false);
        if (!isExisted) {
          BeanUtils.copyProperties(newVisit, visit);
          visit.setSubject(s);
          visitRepo.save(visit);
        }
      }
    }
  }

  public SimpleMailMessage createVisitEmail(Visit visit) {
    SimpleMailMessage message = new SimpleMailMessage();

    Subject s = visit.getSubject();
    CaseStudy c = s.getCaseStudy();
    List<Contraindication> contraindications =
        contraindicationRepo.findAllByCaseStudy(c);
    message.setSubject("Contraindication Suspected Visit");
    message.setText( //
        "•姓名: " + s.getName() + "\n" //
            + "•醫院病歷號: " + s.getPatientId() + "\n" //
            // + "Date: " + visit.getDate() + "\n" //
            + "•看診科別: " + visit.getDivision() + "\n" //
            + "•看診醫師: " + visit.getDoctor() + "\n" //
            + "•網頁連接: " + "https://gcrc.ndmctsgh.edu.tw:8443/cases/" + c.getId()
            + "/subjects/" + s.getId() + "/visits" //
            + "•開立禁忌用藥: "
            + Ruby.Array.of(contraindications)
                .select(cd -> Objects.equals(cd.getBundle(),
                    s.getContraindicationBundle()))
                .map(cd -> cd.getPhrase() + cd.getTakekinds()).join(", ")
            + "\n"
            // + "Room: " + visit.getRoom() + "\n" //
            // + "ContraindicationSuspected: "
            // + visit.isContraindicationSuspected() + "\n" //
            + "------------------------------" //
    );

    return message;
  }

  public List<String> sendVisitEmails() {
    List<String> results = new ArrayList<>();

    List<CaseStudy> cases = caseStudyRepo.findByStatus(Status.EXEC);
    for (CaseStudy c : cases) {
      List<String> messages = new ArrayList<>();

      if (c.getEmails().isEmpty()) {
        String msg = "No email list on CaseStudy[" + c.getCaseNumber() + "]";
        results.add(msg);
        log.info(msg);
        continue;
      }

      List<Subject> subjects = subjectRepo.findAllByCaseStudy(c);
      for (Subject s : subjects) {
        if (s.unreviewedVisits() <= 0) continue;

        s.getVisits().stream().filter(p -> !p.isReviewed()).forEach(v -> {
          SimpleMailMessage message = createVisitEmail(v);
          messages.add(message.getText());
        });
      }

      if (messages.isEmpty()) {
        String msg =
            "No unreviewed visits on CaseStudy[" + c.getCaseNumber() + "]";
        results.add(msg);
        log.info(msg);
      } else {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("gcrc@mail.ndmctsgh.edu.tw");
        message.setSubject("CRC Manager 【看診通知】");
        String prefix = "此訊息為提醒您臨床試驗計劃: 『" + c.getTrialName() + "』的受試者\n\n";
        message.setText(prefix + Ruby.Array.of(messages).join("\n"));
        message.setTo(c.getEmails().toArray(new String[c.getEmails().size()]));

        try {
          emailSender.send(message);
          String msg = "Email of " + messages.size()
              + " visits has been sent to following addresses: " + c.getEmails()
              + " on CaseStudy[" + c.getCaseNumber() + "]";
          results.add(msg);
          log.info(msg);
        } catch (Exception e) {
          String msg = "Failed to send visit email to following addresses: "
              + c.getEmails() + " on CaseStudy[" + c.getCaseNumber() + "]";
          results.add(msg);
          log.error(msg, e);
        }
      }
    }

    return results;
  }

}
