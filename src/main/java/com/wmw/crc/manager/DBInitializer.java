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

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.wmw.crc.manager.model.Medicine;
import com.wmw.crc.manager.repository.MedicineRepository;

@Profile("prod")
@Component
public class DBInitializer {

  @Autowired
  MedicineRepository medicineRepo;

  @PostConstruct
  void init() {
    if (medicineRepo.count() == 0) {
      Medicine m1 = new Medicine();
      m1.setName("普拿疼");
      m1.setEngName("Acetaminophen");
      m1.setAtcCode1("N02BE01");
      medicineRepo.save(m1);
    }
  }

}
