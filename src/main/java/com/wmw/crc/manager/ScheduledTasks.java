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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.github.wnameless.advancedoptional.AdvOpt;
import com.wmw.crc.manager.service.VisitService;
import com.wmw.crc.manager.service.tsgh.TsghService;
import com.wmw.crc.manager.service.tsgh.TsghService.ContraindicationRefreshResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ScheduledTasks {

  @Autowired
  TsghService tsghService;

  @Autowired
  VisitService visitService;

  @Scheduled(cron = "0 0 22 * * *")
  void refreshMedicines() {
    AdvOpt<Integer> opt = tsghService.refreshMedicines();
    if (opt.isAbsent()) log.warn(opt.getMessage());
  }

  @Scheduled(cron = "0 0 9,14,18  * * *")
  void refreshContraindications() {
    AdvOpt<ContraindicationRefreshResult> opt =
        tsghService.refreshContraindications();
    if (opt.get().getFailedCount() != 0) log.warn(opt.getMessage());
  }

  @Scheduled(cron = "0 15 8 * * *")
  void sendVisitEmails() {
    visitService.sendVisitEmails();
  }

  @Scheduled(cron = "0 15 9-21 * * *")
  void sendHourlyVisitEmails() {
    visitService.sendHourlyVisitEmails();
  }

}
