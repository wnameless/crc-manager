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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.github.wnameless.advancedoptional.AdvOpt;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.CaseStudy.Status;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.model.Visit;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.repository.SubjectRepository;
import com.wmw.crc.manager.service.TsghService;
import com.wmw.crc.manager.service.TsghService.ContraindicationRefreshResult;
import com.wmw.crc.manager.service.VisitService;
import com.wmw.crc.manager.util.SubjectVisitUtils;

import lombok.extern.slf4j.Slf4j;
import net.sf.rubycollect4j.Ruby;

@Slf4j
@Component
public class ScheduledTasks {

  @Autowired
  TsghService tsghService;

  @Autowired
  VisitService visitService;

  @Autowired
  JavaMailSender emailSender;

  @Autowired
  CaseStudyRepository caseStudyRepo;

  @Autowired
  SubjectRepository subjectRepo;

  @Scheduled(cron = "0 0 22 * * *")
  void refreshMedicines() {
    AdvOpt<Integer> opt = tsghService.refreshMedicines();
    if (opt.isAbsent()) log.warn(opt.getMessage());
  }

  @Scheduled(cron = "0 0 23 * * *")
  void refreshContraindications() {
    AdvOpt<ContraindicationRefreshResult> opt =
        tsghService.refreshContraindications();
    if (opt.get().getFailedCount() != 0) log.warn(opt.getMessage());
  }

  @Scheduled(cron = "0 0 8-21 * * *")
  void sendVisitEmails() {
    List<CaseStudy> cases = caseStudyRepo.findByStatus(Status.EXEC);

    for (CaseStudy c : cases) {
      if (c.getEmails().isEmpty()) {
        log.info("No email list on CaseStudy[" + c.getTrialName() + "]");
        continue;
      }

      List<Subject> subjects = subjectRepo.findAllByCaseStudy(c);
      List<String> messages = new ArrayList<>();
      for (Subject s : subjects) {
        if (s.unreviewedVisits() <= 0) continue;

        for (Visit v : SubjectVisitUtils.trimVisits(s.getVisits())) {
          if (v.isReviewed()) continue;

          SimpleMailMessage message = visitService.createVisitEmail(v);
          messages.add(message.getText());
        }
      }

      if (messages.isEmpty()) {
        log.info("No unreviewed visits on CaseStudy[" + c.getTrialName() + "]");
      } else {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(
            "Contraindication Suspected Visits (" + messages.size() + ")");
        message.setText(Ruby.Array.of(messages).join("\n"));
        message.setTo(c.getEmails().toArray(new String[messages.size()]));

        try {
          emailSender.send(message);
          log.info("Email of " + messages.size()
              + " visits has been sent to following addresses: " + c.getEmails()
              + " on CaseStudy[" + c.getTrialName() + "]");
        } catch (Exception e) {
          log.error(
              "Failed to send visit email to following addresses: "
                  + c.getEmails() + " on CaseStudy[" + c.getTrialName() + "]",
              e);
        }
      }
    }
  }

}
