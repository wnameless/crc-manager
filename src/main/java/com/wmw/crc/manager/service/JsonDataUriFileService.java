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
package com.wmw.crc.manager.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.JsonDataUriFile;
import com.wmw.crc.manager.repository.JsonDataUriFileRepository;

@Service
public class JsonDataUriFileService {

  @Autowired
  JsonDataUriFileRepository jsonDataUriFileRepo;

  public List<JsonDataUriFile> createOrUpdate(CaseStudy cs,
      Map<String, String> dataURIs) {
    List<JsonDataUriFile> jsonDataUriFiles = new ArrayList<>();

    for (Entry<String, String> dataURI : dataURIs.entrySet()) {
      Optional<JsonDataUriFile> fileOpt =
          jsonDataUriFileRepo.findByCaseStudyAndJsonKey(cs, dataURI.getKey());

      if (fileOpt.isPresent()) {
        JsonDataUriFile file = fileOpt.get();

        file.setData(
            new JsonDataUriFile(cs, dataURI.getKey(), dataURI.getValue())
                .getData());

        jsonDataUriFiles.add(jsonDataUriFileRepo.save(file));
      } else {
        jsonDataUriFiles.add(jsonDataUriFileRepo.save(
            new JsonDataUriFile(cs, dataURI.getKey(), dataURI.getValue())));
      }
    }

    return jsonDataUriFiles;
  }

}
