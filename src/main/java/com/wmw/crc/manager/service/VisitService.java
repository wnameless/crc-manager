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

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.wmw.crc.manager.controller.api.NewVisit;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.CaseStudy.Status;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.model.Visit;
import com.wmw.crc.manager.repository.CaseStudyRepository;
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
  JavaMailSender emailSender;

  @Autowired
  CaseStudyRepository caseStudyRepo;
  @Autowired
  SubjectRepository subjectRepo;
  @Autowired
  VisitRepository visitRepo;

  @Autowired
  CaseStudyService caseService;
  @Autowired
  SubjectService subjectService;
  @Autowired
  I18nService i18n;

  public void addVisit(NewVisit newVisit) {
    List<Subject> subjects =
        subjectService.findOngoingSubjects(newVisit.getNationalId());

    for (Subject s : subjects) {
      boolean isAddable = newVisit.isContraindicationSuspected() || //
          !(visitRepo
              .existsBySubjectAndDivisionAndDoctorAndRoomAndDateAndContraindicationSuspected(
                  s, newVisit.getDivision(), newVisit.getDoctor(),
                  newVisit.getRoom(), newVisit.getDate(), false));

      if (isAddable) {
        Visit visit = new Visit();
        BeanUtils.copyProperties(newVisit, visit);
        visit.setSubject(s);

        // Add unreviewed visit count to CaseStudy
        CaseStudy cs = s.getCaseStudy();
        cs.setUnreviewedOngoingVisits(cs.getUnreviewedOngoingVisits() + 1);

        s.getVisits().add(visit);
        subjectRepo.save(s);
        // visitRepo.save(visit);
      }
    }
  }

  public boolean reviewVisit(Subject subject, Long visitId) {
    CaseStudy caseStudy = subject.getCaseStudy();

    Visit visit =
        Ruby.Array.of(subject.getVisits()).find(v -> v.getId().equals(visitId));
    visit.setReviewed(!visit.isReviewed());

    // Recount unreviewed visit count to CaseStudy
    long count = visit.isReviewed() ? -1 : 1;
    long unreviewedOngoingVisits =
        caseStudy.getUnreviewedOngoingVisits() + count;
    unreviewedOngoingVisits =
        unreviewedOngoingVisits < 0 ? 0 : unreviewedOngoingVisits;
    caseStudy.setUnreviewedOngoingVisits(unreviewedOngoingVisits);
    // caseStudyRepo.save(caseStudy);

    subjectRepo.save(subject);
    // visitRepo.save(visit);

    return visit.isReviewed();
  }

  public SimpleMailMessage createVisitEmail(Visit visit, boolean includeCD) {
    SimpleMailMessage message = new SimpleMailMessage();

    Subject s = visit.getSubject();
    CaseStudy c = s.getCaseStudy();
    message.setSubject("Contraindication Suspected Visit");
    message.setText( //
        "•姓名: " + s.getName() + "\n" //
            + "•醫院病歷號: " + s.getPatientId() + "\n" //
            + "•看診科別: " + visit.getDivision() + "\n" //
            + "•看診醫師: " + visit.getDoctor() + "\n" //
            + "•網頁連接: " + "https://gcrc.ndmctsgh.edu.tw:8443/cases/" + c.getId()
            + "/subjects/" + s.getId() + "/visits" + "\n" //
            + (includeCD
                ? "•開立禁忌用藥: "
                    + (visit.isContraindicationSuspected() ? "是" : "否") + "\n"
                : "") //
            // + Ruby.Array.of(contraindicationRepo.findAllByCaseStudy(c))
            // // 符合用藥組別
            // .select(cd -> Objects.equals(cd.getBundle(),
            // s.getContraindicationBundle()))
            // // 將藥物用法代號轉文字
            // .map(cd -> cd.getPhrase() + Ruby.Array.of(cd.getTakekinds())
            // .map(tk -> i18n.takeKind(tk)))
            // .join(", ")
            + "------------------------------" //
    );

    return message;
  }

  public List<String> sendVisitEmails() {
    List<String> results = new ArrayList<>();

    List<CaseStudy> cases = caseStudyRepo.findAllByStatus(Status.EXEC);
    for (CaseStudy c : cases) {
      List<String> visitMessages = new ArrayList<>();
      List<String> contraindicationMessages = new ArrayList<>();

      if (c.getEmails().isEmpty()) {
        String msg = "No email list on CaseStudy[" + c.getIrbNumber() + "]";
        results.add(msg);
        log.info(msg);
        continue;
      }

      List<Subject> subjects = subjectService.findOngoingSubjects(c);
      for (Subject s : subjects) {
        if (s.unreviewedVisits() <= 0) continue;

        s.getVisits().stream().filter(p -> !p.isReviewed()).forEach(v -> {
          SimpleMailMessage message;
          if (v.isContraindicationSuspected()) {
            message = createVisitEmail(v, true);
            contraindicationMessages.add(message.getText());
          } else {
            message = createVisitEmail(v, false);
            visitMessages.add(message.getText());
          }
        });
      }

      if (visitMessages.isEmpty() && contraindicationMessages.isEmpty()) {
        String msg =
            "No unreviewed visits on CaseStudy[" + c.getIrbNumber() + "]";
        results.add(msg);
        log.info(msg);
      } else {
        if (!visitMessages.isEmpty()) {
          SimpleMailMessage message = new SimpleMailMessage();
          message.setFrom("gcrc@mail.ndmctsgh.edu.tw");
          message.setSubject("CRC Manager【看診通知】");
          String prefix = "此訊息為提醒您臨床試驗計劃: 『" + c.getTrialName() + "』的受試者\n\n";
          message.setText(prefix + Ruby.Array.of(visitMessages).join("\n"));
          message
              .setTo(c.getEmails().toArray(new String[c.getEmails().size()]));

          try {
            emailSender.send(message);
            String msg = "Email of " + visitMessages.size()
                + " visits has been sent to following addresses: "
                + c.getEmails() + " on CaseStudy[" + c.getIrbNumber() + "]";
            results.add(msg);
            log.info(msg);
          } catch (Exception e) {
            String msg = "Failed to send visit email to following addresses: "
                + c.getEmails() + " on CaseStudy[" + c.getIrbNumber() + "]";
            results.add(msg);
            log.error(msg, e);
          }
        }
        if (!contraindicationMessages.isEmpty()) {
          SimpleMailMessage message = new SimpleMailMessage();
          message.setFrom("gcrc@mail.ndmctsgh.edu.tw");
          message.setSubject("CRC Manager【禁忌用藥開立通知】");
          String prefix = "此訊息為提醒您臨床試驗計劃: 『" + c.getTrialName() + "』的受試者\n\n";
          message.setText(
              prefix + Ruby.Array.of(contraindicationMessages).join("\n"));
          message
              .setTo(c.getEmails().toArray(new String[c.getEmails().size()]));

          try {
            emailSender.send(message);
            String msg = "Email of " + contraindicationMessages.size()
                + " contraindications has been sent to following addresses: "
                + c.getEmails() + " on CaseStudy[" + c.getIrbNumber() + "]";
            results.add(msg);
            log.info(msg);
          } catch (Exception e) {
            String msg =
                "Failed to send contraindication email to following addresses: "
                    + c.getEmails() + " on CaseStudy[" + c.getIrbNumber() + "]";
            results.add(msg);
            log.error(msg, e);
          }
        }
      }
    }

    return results;
  }

  public void reCountCaseStudyURVs() {
    for (CaseStudy caseStudy : caseStudyRepo.findAll()) {
      long urvCount = 0L;

      for (Subject subject : subjectRepo.findAllByCaseStudy(caseStudy)) {
        urvCount += subject.unreviewedVisits();
      }

      caseStudy.setUnreviewedOngoingVisits(urvCount);
      caseStudyRepo.save(caseStudy);
    }
  }

}
