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

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
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
    return Ruby.Array.of(kc.realm("CRCManager").users().list()).deleteIf(
        u -> u.getUsername().equals("super") || u.getUsername().equals("admin"))
        .toList();
  }

  public void addOrCreateUser(UserRepresentation user) {
    RealmResource realmResource = kc.realm("CRCManager");
    UsersResource userRessource = realmResource.users();

    // Create user (requires manage-users role)
    Response response = userRessource.create(user);
    System.out.println("Repsonse: " + response.getStatusInfo());
    System.out.println(response.getLocation());
    String userId =
        response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

    System.out.printf("User created with userId: %s%n", userId);

    // Get realm role "tester" (requires view-realm role)
    RoleRepresentation testerRealmRole = realmResource.roles()//
        .get("tester").toRepresentation();

    // Assign realm role tester to user
    userRessource.get(userId).roles().realmLevel() //
        .add(Arrays.asList(testerRealmRole));

    // Get client
    ClientRepresentation app1Client = realmResource.clients() //
        .findByClientId("crc-manager").get(0);

    // Get client level role (requires view-clients role)
    RoleRepresentation userClientRole =
        realmResource.clients().get(app1Client.getId()) //
            .roles().get("user").toRepresentation();

    // Assign client level role to user
    userRessource.get(userId).roles() //
        .clientLevel(app1Client.getId()).add(Arrays.asList(userClientRole));

    // Define password credential
    CredentialRepresentation passwordCred = new CredentialRepresentation();
    passwordCred.setTemporary(false);
    passwordCred.setType(CredentialRepresentation.PASSWORD);
    passwordCred.setValue("test");

    // Set password credential
    userRessource.get(userId).resetPassword(passwordCred);
  }

}