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
package com.wmw.crc.manager.service;

import java.io.IOException;
import java.util.List;

import com.github.wnameless.advancedoptional.AdvOpt;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.service.tsgh.api.Drug;
import com.wmw.crc.manager.service.tsgh.api.PatientContraindication;

import lombok.Data;
import okhttp3.ResponseBody;
import retrofit2.Response;

public interface TsghService {

  public Subject queryPatientById(String nationalId) throws IOException;

  public List<Drug> getDrugs() throws IOException;

  public Response<ResponseBody> addPatientContraindication(
      PatientContraindication pc) throws IOException;

  public AdvOpt<Integer> refreshMedicines();

  public AdvOpt<ContraindicationRefreshResult> refreshContraindications();

  @Data
  public static final class ContraindicationRefreshResult {

    int successCount = 0;

    int failedCount = 0;

    public int increaseSuccessCount() {
      return ++successCount;
    }

    public int increaseFailedCount() {
      return ++failedCount;
    }

  }

}
