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
package com.wmw.crc.manager.controller.api;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.service.KeycloakService;

import net.sf.rubycollect4j.Ruby;

@RequestMapping("/api/1.0/ptms")
@RestController
public class PtmsApiController {

  @Autowired
  CaseStudyRepository caseRepo;

  @Autowired
  KeycloakService keycloak;

  Gson gson = new Gson();

  @RequestMapping(path = "/protocols", method = RequestMethod.GET)
  List<Protocol> getAllCases() {
    return newArrayList(Ruby.Array.of(caseRepo.findAll()).map(c -> {
      Protocol proc = new Protocol();
      proc.setProtocolNumber(c.getProjectNumber());
      proc.setJsonData(newArrayList(c.getFormData()));
      return proc;
    }));
  }

  @RequestMapping(path = "/protocols", method = RequestMethod.POST)
  String newCase(@RequestBody Protocol protocol) throws IOException {
    CaseStudy c = new CaseStudy();
    c.setFormData(protocol.getJsonData().get(0));

    c.setOwner(protocol.getOwner().toLowerCase());
    if (protocol.getManagers() != null) {
      c.getManagers().addAll(protocol.getManagers().stream()
          .map(String::toLowerCase).collect(Collectors.toSet()));
    }
    if (protocol.getEditors() != null) {
      c.getEditors().addAll(protocol.getEditors().stream()
          .map(String::toLowerCase).collect(Collectors.toSet()));
    }
    if (protocol.getViewers() != null) {
      c.getViewers().addAll(protocol.getViewers().stream()
          .map(String::toLowerCase).collect(Collectors.toSet()));
    }
    caseRepo.save(c);

    return "New ok";
  }

  @RequestMapping(path = "/protocols/{irbNumber}", method = RequestMethod.POST)
  String updateCase(@RequestBody Protocol protocol,
      @PathVariable String irbNumber) {
    CaseStudy c = caseRepo.findByIrbNumber(irbNumber);

    ObjectNode objNode = new ObjectMapper().createObjectNode();
    objNode.setAll((ObjectNode) c.getFormData());
    objNode.setAll((ObjectNode) protocol.getJsonData().get(0));
    c.setFormData(objNode);

    // c.setFormData(protocol.getJsonData().get(0));
    caseRepo.save(c);

    return "Update ok";
  }

  @RequestMapping(path = "/users", method = RequestMethod.POST)
  String createUser(@RequestBody KeycloakUser user) {
    UserRepresentation ur = new UserRepresentation();

    ur.setUsername(user.getUsername().toLowerCase());
    ur.setEmail(user.getEmail());
    ur.setFirstName(user.getFirstName());
    ur.setLastName(user.getLastName());
    ur.setEnabled(true);

    if (keycloak.addOrCreateUser(ur))
      return "User created";
    else
      return "User existed";
  }

}
