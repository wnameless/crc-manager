/*
 *
 * Copyright 2019 Wei-Ming Wu
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
package com.wmw.crc.manager.controller.api;

import java.util.List;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.model.Visit;
import com.wmw.crc.manager.repository.VisitRepository;
import com.wmw.crc.manager.service.CrcManagerService;

@RequestMapping("/api/1.0/tsgh")
@RestController
public class TsghApiController {

  @Autowired
  CrcManagerService crcManagerService;

  @Autowired
  VisitRepository visitRepo;

  @Autowired
  JavaMailSender emailSender;

  @Autowired
  Environment env;

  @RequestMapping(path = "/visits", method = RequestMethod.POST)
  String addVisit(@RequestBody NewVisit newVisit) {
    List<Subject> subjects =
        crcManagerService.findExecSubjects(newVisit.getNationalId()).get();

    for (Subject s : subjects) {
      Visit visit = new Visit();
      BeanUtils.copyProperties(newVisit, visit);
      visit.setSubject(s);
      visitRepo.save(visit);

      String email1 = s.getCaseStudy().getPiEmail1();

      if (env.acceptsProfiles(Profiles.of("email"))) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("Contraindication Suspected");
        message.setText("Name: " + s.getName() + "\n" //
            + "NationalID: " + s.getNationalId() + "\n" //
            + "Date: " + visit.getDate() + "\n" //
            + "Division: " + visit.getDivision() + "\n" //
            + "Doctor: " + visit.getDoctor() + "\n" //
            + "Room: " + visit.getRoom() + "\n" //
            + "ContraindicationSuspected: "
            + visit.isContraindicationSuspected() + "\n" //
        );
        if (Strings.isNotBlank(email1)) {
          message.setTo(email1);
          emailSender.send(message);
        }
      }
    }

    return "Visit added";
  }

}
