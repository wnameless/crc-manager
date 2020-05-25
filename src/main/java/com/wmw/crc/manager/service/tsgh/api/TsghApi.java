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
package com.wmw.crc.manager.service.tsgh.api;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TsghApi {

  @GET("patients")
  Call<TsghResponse<TsghPatient>> searchPatient(
      @Query("nationalId") String nationalId);

  @GET("drugs")
  Call<TsghResponse<List<Drug>>> listDrugs();

  @POST("Contraindications")
  Call<ResponseBody> addPatientContraindication(
      @Body PatientContraindication pc);

}
