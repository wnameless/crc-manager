package com.wmw.crc.manager.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.Contraindication;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.service.CaseStudyService;

@Controller
public class ContraindicationController {

  @Autowired
  CaseStudyService caseStudyService;

  @Autowired
  CaseStudyRepository caseRepo;

  @PreAuthorize("@perm.canRead(#id)")
  @GetMapping("/cases/{id}/contraindications")
  String index(Model model, @PathVariable("id") Long id) {
    CaseStudy cs = caseRepo.findById(id).get();

    List<Contraindication> cds =
        caseStudyService.getSortedContraindications(cs);

    model.addAttribute("case", cs);
    model.addAttribute("contraindications", cds);
    return "contraindication/index";
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @PostMapping("cases/{id}/contraindications")
  String add(Model model, @PathVariable("id") Long id,
      @RequestParam("bundle") Integer bundle,
      @RequestParam("phrase") String phrase,
      @RequestParam("takekinds") List<String> takekinds,
      @RequestParam("memo") String memo) {
    CaseStudy cs = caseRepo.findById(id).get();

    caseStudyService.addContraindication(cs, bundle, phrase, takekinds, memo);

    return "redirect:/cases/" + id + "/contraindications";
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @GetMapping("cases/{id}/contraindications/{cdId}")
  String remove(Model model, @PathVariable("id") Long id,
      @PathVariable("cdId") Long cdId) {
    CaseStudy cs = caseRepo.findById(id).get();

    caseStudyService.removeContraindication(cs, cdId);

    return "redirect:/cases/" + id + "/contraindications";
  }

}
