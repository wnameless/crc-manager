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
package com.wmw.crc.manager.controller.api;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wmw.crc.manager.model.Case;
import com.wmw.crc.manager.model.CaseSupplement1;
import com.wmw.crc.manager.model.CaseSupplement2;
import com.wmw.crc.manager.repository.CaseRepository;

import net.sf.rubycollect4j.Ruby;

@RequestMapping("/api/1.0/ptms")
@RestController
public class ApiController {

  @Autowired
  CaseRepository caseRepo;

  Gson gson = new Gson();

  @RequestMapping(path = "/protocols", method = RequestMethod.GET)
  List<Protocol> getAllCases() {
    return newArrayList(Ruby.Array.of(caseRepo.findAll()).map(c -> {
      Protocol proc = new Protocol();
      proc.setProtocolNumber(c.getProjectNumber());

      Map<String, Object> m1 = gson.fromJson(c.getJsonData(),
          new TypeToken<Map<String, Object>>() {}.getType());
      Map<String, Object> m2 = gson.fromJson(c.getSupplement1().getJsonData(),
          new TypeToken<Map<String, Object>>() {}.getType());
      Map<String, Object> m3 = gson.fromJson(c.getSupplement2().getJsonData(),
          new TypeToken<Map<String, Object>>() {}.getType());

      proc.setJsonData(newArrayList(m1, m2, m3));
      return proc;
    }));
  }

  @RequestMapping(path = "/protocols", method = RequestMethod.POST)
  String newCase(@RequestBody Protocol protocol) throws IOException {
    Case c = new Case();
    c.setJsonData(gson.toJson(protocol.getJsonData().get(0)));
    URL url = Resources.getResource("json-schema/新進案件區-part1-JSONSchema.json");
    c.setJsonSchema(Resources.toString(url, UTF_8));
    url = Resources.getResource("json-schema/新進案件區-part1-UISchema.json");
    c.setJsonUiSchema(Resources.toString(url, UTF_8));

    com.wmw.crc.manager.model.CRC crc = new com.wmw.crc.manager.model.CRC();
    c.setCrc(crc);

    CaseSupplement1 cs1 = new CaseSupplement1();
    c.setSupplement1(cs1);

    CaseSupplement2 cs2 = new CaseSupplement2();
    url = Resources.getResource("json-schema/新進案件區-part3-JSONSchema.json");
    cs2.setJsonSchema(Resources.toString(url, UTF_8));
    url = Resources.getResource("json-schema/新進案件區-part3-UISchema.json");
    cs2.setJsonUiSchema(Resources.toString(url, UTF_8));
    c.setSupplement2(cs2);
    c.getSupplement2().setJsonData(gson.toJson(protocol.getJsonData().get(2)));
    caseRepo.save(c);

    return "New ok";
  }

  @RequestMapping(path = "/protocols/{protocolNumber}",
      method = RequestMethod.POST)
  String updateCase(@RequestBody Protocol protocol,
      @PathVariable("protocolNumber") String protocolNumber) {

    Case c = caseRepo.findByCaseNumber(protocolNumber);
    c.setJsonData(gson.toJson(protocol.getJsonData().get(0)));
    c.getSupplement1().setJsonData(gson.toJson(protocol.getJsonData().get(1)));
    c.getSupplement2().setJsonData(gson.toJson(protocol.getJsonData().get(2)));
    caseRepo.save(c);

    return "Update ok";
  }

}
