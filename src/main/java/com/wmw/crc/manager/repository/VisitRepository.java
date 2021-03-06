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
package com.wmw.crc.manager.repository;

import java.time.LocalDate;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.model.Visit;

@JaversSpringDataAuditable
@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

  boolean existsBySubjectAndDivisionAndDoctorAndRoomAndDateAndContraindicationSuspected(
      Subject subject, String division, String doctor, String room,
      LocalDate date, boolean contraindicationSuspected);

}
