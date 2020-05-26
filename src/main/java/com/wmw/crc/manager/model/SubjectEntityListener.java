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
package com.wmw.crc.manager.model;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import org.springframework.beans.factory.annotation.Autowired;

import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.repository.SubjectRepository;
import com.wmw.crc.manager.service.SubjectService;
import com.wmw.crc.manager.util.AutowireHelper;

public class SubjectEntityListener {

  @Autowired
  CaseStudyRepository caseStudyRepo;

  @Autowired
  SubjectRepository subjectRepo;

  @Autowired
  SubjectService subjectService;

  @PostPersist
  public void postPersist(Subject target) {
    AutowireHelper.autowire(this, caseStudyRepo);
  }

  @PostUpdate
  public void postUpdate(Subject target) {
    AutowireHelper.autowire(this, caseStudyRepo);
  }

  @PostRemove
  public void postDelete(Subject target) {
    AutowireHelper.autowire(this, caseStudyRepo);
  }

}
