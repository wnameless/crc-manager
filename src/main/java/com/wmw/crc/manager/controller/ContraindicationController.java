package com.wmw.crc.manager.controller;

import static com.wmw.crc.manager.model.RestfulModel.Names.CASE_STUDY;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.wnameless.spring.common.RestfulController;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.Contraindication;
import com.wmw.crc.manager.model.RestfulModel;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.service.CaseStudyService;

@RequestMapping("/" + CASE_STUDY)
@Controller
public class ContraindicationController implements
    RestfulController<CaseStudy, Long, CaseStudyRepository, RestfulModel> {

  @Autowired
  CaseStudyService caseStudyService;

  @Autowired
  CaseStudyRepository caseRepo;

  CaseStudy caseStudy;

  Model model;

  @ModelAttribute
  void init(Model model, @PathVariable(required = false) Long id) {
    this.model = model;

    caseStudy = getItem(id, new CaseStudy());
  }

  @PreAuthorize("@perm.canRead(#id)")
  @GetMapping("/{id}/contraindications")
  String index(@PathVariable("id") Long id) {
    List<Contraindication> cds =
        caseStudyService.getSortedContraindications(caseStudy);

    model.addAttribute("contraindications", cds);
    return "contraindication/index";
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @PostMapping("/{id}/contraindications")
  String add(@PathVariable("id") Long id,
      @RequestParam("bundle") Integer bundle,
      @RequestParam("phrase") String phrase,
      @RequestParam("takekinds") List<String> takekinds,
      @RequestParam("memo") String memo) {
    caseStudyService.addContraindication(caseStudy, bundle, phrase, takekinds,
        memo);

    return "redirect:" + caseStudy.joinPath("contraindications");
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @GetMapping("/{id}/contraindications/{cdId}")
  String remove(@PathVariable("id") Long id, @PathVariable("cdId") Long cdId) {
    caseStudyService.removeContraindication(caseStudy, cdId);

    return "redirect:" + caseStudy.joinPath("contraindications");
  }

  @Override
  public RestfulModel getRoute() {
    return RestfulModel.CASE_STUDY;
  }

  @Override
  public CaseStudyRepository getRepository() {
    return caseRepo;
  }

}
