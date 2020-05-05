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

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.wmw.crc.manager.controller.api.NewVisit;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.model.Visit;
import com.wmw.crc.manager.repository.VisitRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class VisitService {

  @Autowired
  Environment env;

  @Autowired
  CrcManagerService crcManagerService;

  @Autowired
  VisitRepository visitRepo;

  @Autowired
  JavaMailSender emailSender;

  // @Value("${contraindication.manager.mail}")
  // String contraindicationManagerMail;

  public void addVisit(NewVisit newVisit) {
    List<Subject> subjects =
        crcManagerService.findExecSubjects(newVisit.getNationalId()).get();

    for (Subject s : subjects) {
      Visit visit = new Visit();
      BeanUtils.copyProperties(newVisit, visit);
      visit.setSubject(s);
      visitRepo.save(visit);
    }
  }

  public SimpleMailMessage createVisitEmail(Visit visit) {
    SimpleMailMessage message = new SimpleMailMessage();

    Subject s = visit.getSubject();
    message.setSubject("Contraindication Suspected Visit");
    message.setText("Name: " + s.getName() + "\n" //
        + "NationalID: " + s.getNationalId() + "\n" //
        + "Date: " + visit.getDate() + "\n" //
        + "Division: " + visit.getDivision() + "\n" //
        + "Doctor: " + visit.getDoctor() + "\n" //
        + "Room: " + visit.getRoom() + "\n" //
        + "ContraindicationSuspected: " + visit.isContraindicationSuspected()
        + "\n" //
        + "------------------------------" //
    );

    return message;
  }

}
