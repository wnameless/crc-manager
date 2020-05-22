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
package com.wmw.crc.manager.service;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.wnameless.advancedoptional.AdvOpt;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.Contraindication;
import com.wmw.crc.manager.model.Subject;
import com.wmw.crc.manager.model.form.Criterion;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.repository.ContraindicationRepository;
import com.wmw.crc.manager.repository.SubjectRepository;

import net.sf.rubycollect4j.Ruby;
import net.sf.rubycollect4j.RubyArray;

@Service
public class CaseStudyService {

  @Autowired
  CaseStudyRepository caseRepo;

  @Autowired
  SubjectRepository subjectRepo;

  @Autowired
  ContraindicationRepository contraindicationRepo;

  @Autowired
  JsonDataExportService dataExport;

  @PersistenceContext
  EntityManager em;

  public HttpEntity<byte[]> getDownloadableFile(CaseStudy cs, String fileId) {
    JsonNode formData = cs.getFormData();
    String base64 = formData.get("requiredFiles").get(fileId).textValue();
    String[] base64Array = base64.split(";");

    String type = base64Array[0].substring(5);
    String name = base64Array[1].substring(5);
    String data = base64Array[2].substring(7);

    byte[] dataByteArray = Base64.decodeBase64(data.getBytes());

    HttpHeaders header = new HttpHeaders();
    header.setContentType(MediaType.valueOf(type));
    header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + name);
    header.setContentLength(dataByteArray.length);

    return new HttpEntity<byte[]>(dataByteArray, header);
  }

  public Map<String, Entry<String, Boolean>> getFilesFromCaseStudy(
      CaseStudy cs) {
    Map<String, Entry<String, Boolean>> files = new LinkedHashMap<>();

    JsonNode schema = cs.getSchema();
    JsonNode formData = cs.getFormData();
    JsonNode fileNode =
        schema.get("properties").get("requiredFiles").get("properties");
    for (String fileId : Ruby.Array.copyOf(fileNode.fieldNames())) {
      String fileTitle = fileNode.get(fileId).get("title").textValue();
      JsonNode requiredFiles = formData.get("requiredFiles");
      JsonNode requiredFile = null;
      if (requiredFiles != null) {
        requiredFile = requiredFiles.get(fileId);
      }
      files.put(fileId, new SimpleEntry<>(fileTitle, requiredFile != null));
    }

    return files;
  }

  public Iterable<CaseStudy> getCasesByStatus(Authentication auth,
      CaseStudy.Status status) {
    return caseRepo.findAllByUserAndStatus(auth, status);
  }

  public Page<CaseStudy> getCasesByStatus(Authentication auth,
      CaseStudy.Status status, Pageable pageable) {
    return caseRepo.findAllByUserAndStatus(auth, status, pageable);
  }

  public Page<CaseStudy> getCasesByStatus(Authentication auth,
      CaseStudy.Status status, Pageable pageable, String search) {
    if (search != null && !search.isEmpty()) {
      return caseRepo.findAllByUserAndStatus(auth, status, pageable, search);
    }

    return getCasesByStatus(auth, status, pageable);
  }

  public List<Contraindication> getSortedContraindications(CaseStudy cs) {
    return contraindicationRepo.findAllByCaseStudy(cs, Sort.by("bundle"));
  }

  public AdvOpt<Contraindication> addContraindication(CaseStudy cs,
      Integer bundle, String phrase, List<String> takekinds, String memo) {
    if (!isNullOrEmpty(phrase)) {
      Contraindication cd = new Contraindication();
      cd.setCaseStudy(cs);
      cd.setBundle(bundle);
      cd.setPhrase(phrase);
      cd.setTakekinds(takekinds);
      cd.setMemo(memo);
      contraindicationRepo.save(cd);

      return AdvOpt.of(cd);
    } else {
      return AdvOpt.ofNullable(null);
    }
  }

  public AdvOpt<Contraindication> removeContraindication(CaseStudy cs,
      Long cdId) {
    List<Contraindication> cds = contraindicationRepo.findAllByCaseStudy(cs);
    Contraindication target =
        Ruby.Array.of(cds).find(cd -> Objects.equals(cdId, cd.getId()));

    if (target != null) {
      contraindicationRepo.delete(target);
      return AdvOpt.of(target);
    } else {
      return AdvOpt.ofNullable(null);
    }
  }

  public List<CaseStudy> searchCaseStudies(Authentication auth,
      List<Criterion> criteria) {
    RubyArray<CaseStudy> targets = null;

    List<Criterion> caseCriteria = Ruby.Array.of(criteria)
        .select(c -> Objects.equals(c.getType(), "CaseStudy"));
    List<Criterion> subjectCriteria = Ruby.Array.of(criteria)
        .select(c -> Objects.equals(c.getType(), "Subject"));

    Iterable<CaseStudy> readableCases;
    if (caseCriteria.isEmpty()) {
      readableCases = caseRepo.findAllByUser(auth);
    } else {
      readableCases = caseRepo.findAllByUserAndCriteria(auth, caseCriteria);
    }

    if (subjectCriteria.isEmpty()) {
      targets = Ruby.Enumerator.of(readableCases).toA();
    } else {
      targets = Ruby.Array.of(subjectRepo
          .findAllByCaseStudyInAndCriteria(readableCases, subjectCriteria))
          .map(Subject::getCaseStudy).uniq();
    }

    return targets.toList();
  }

  public HttpEntity<byte[]> createDownloadableExcelCaseStudy(Long id,
      Locale locale) throws IOException {
    CaseStudy kase = caseRepo.findById(id).get();

    Workbook wb = dataExport.toExcel(kase, locale);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    wb.write(bos);
    wb.close();

    byte[] documentBody = bos.toByteArray();

    HttpHeaders header = new HttpHeaders();
    header.setContentType(MediaType.valueOf(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    header.set(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=" + kase.getIrbNumber() + ".xlsx");
    header.setContentLength(documentBody.length);

    return new HttpEntity<byte[]>(documentBody, header);
  }

}
