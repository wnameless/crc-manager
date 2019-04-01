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

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Service
public class TsghApi {

  @Value("${api.tsgh.baseurl}")
  String baseUrl;

  TsghService service;

  @PostConstruct
  void postConstruct() {
    HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
    interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    OkHttpClient client =
        new OkHttpClient.Builder().addInterceptor(interceptor).build();

    Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl).client(client)
        .addConverterFactory(GsonConverterFactory.create()).build();
    service = retrofit.create(TsghService.class);
  }

  public Patient findPatientById(String nationalId) throws IOException {
    Call<Patient> call = service.searchPatient(nationalId);
    Response<Patient> res = call.execute();
    return res.body();
  }

  public List<Drug> getDrugs() throws IOException {
    Call<List<Drug>> call = service.listDrugs();
    Response<List<Drug>> res = call.execute();
    return res.body();
  }

  public ResponseBody addPatientContraindication(PatientContraindication pc)
      throws IOException {
    Call<ResponseBody> call = service.addPatientContraindication(pc);
    Response<ResponseBody> res = call.execute();
    return res.body();
  }

}
