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
package com.wmw.crc.manager.service;

import java.util.List;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import net.sf.rubycollect4j.Ruby;

@Service("keycloak")
public class KeycloakService {

  Keycloak kc =
      KeycloakBuilder.builder().serverUrl("http://120.126.47.32:8081/auth")
          .realm("CRCManager").username("super").password("1qaz@WSX")
          .clientId("crc-manager")
          .resteasyClient(
              new ResteasyClientBuilder().connectionPoolSize(10).build())
          .build();

  public List<UserRepresentation> getNormalUsers() {
    return Ruby.Array.of(kc.realm("CRCManager").users().list())
        .deleteIf(u -> u.getUsername().equals("super")).toList();
  }

  public boolean addOrCreateUser(UserRepresentation user) {
    if (Ruby.Array.of(getNormalUsers()).map(ur -> ur.getUsername())
        .contains(user.getUsername().toLowerCase()))
      return false;

    RealmResource realmResource = kc.realm("CRCManager");
    UsersResource userRessource = realmResource.users();

    // Create user (requires manage-users role)
    Response response = userRessource.create(user);
    String userId =
        response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

    // Define password credential
    CredentialRepresentation passwordCred = new CredentialRepresentation();
    passwordCred.setTemporary(false);
    passwordCred.setType(CredentialRepresentation.PASSWORD);
    passwordCred.setValue("test!TEST");

    // Set password credential
    userRessource.get(userId).resetPassword(passwordCred);

    return true;
  }

}