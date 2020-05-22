package com.wmw.crc.manager.controller;

import static com.wmw.crc.manager.model.RestfulModel.Names.CASE_STUDY;
import static com.wmw.crc.manager.model.RestfulModel.Names.CONTRAINDICATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.wnameless.spring.common.NestedRestfulController;
import com.github.wnameless.spring.common.RestfulRoute;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.Contraindication;
import com.wmw.crc.manager.model.RestfulModel;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.repository.ContraindicationRepository;
import com.wmw.crc.manager.service.CaseStudyService;

@RequestMapping("/" + CASE_STUDY + "/{parentId}/" + CONTRAINDICATION)
@Controller
public class ContraindicationController implements NestedRestfulController< //
    CaseStudy, Long, CaseStudyRepository, RestfulModel, //
    Contraindication, Long, ContraindicationRepository, RestfulModel> {

  @Autowired
  CaseStudyRepository caseRepo;
  @Autowired
  ContraindicationRepository cdRepository;
  @Autowired
  CaseStudyService caseStudyService;

  CaseStudy caseStudy;

  Model model;

  @ModelAttribute
  void init(Model model, @PathVariable Long parentId) {
    this.model = model;

    caseStudy = getParent(parentId);
  }

  @PreAuthorize("@perm.canRead(#parentId)")
  @GetMapping
  String index(@PathVariable Long parentId) {
    return "contraindication/index";
  }

  @PreAuthorize("@perm.canWrite(#parentId)")
  @PostMapping
  String create(@PathVariable Long parentId,
      @RequestParam("bundle") Integer bundle,
      @RequestParam("phrase") String phrase,
      @RequestParam("takekinds") List<String> takekinds,
      @RequestParam("memo") String memo) {
    caseStudyService.addContraindication(caseStudy, bundle, phrase, takekinds,
        memo);

    updateChildren(model, caseStudy);
    return "redirect:" + caseStudy.joinPath("contraindications");
  }

  @PreAuthorize("@perm.canWrite(#parentId)")
  @DeleteMapping(path = "/{id}", produces = APPLICATION_JSON_VALUE)
  String destroyJS(@PathVariable Long parentId, @PathVariable Long id) {
    caseStudyService.removeContraindication(caseStudy, id);

    updateChildren(model, caseStudy);
    return "contraindication/list :: partial";
  }

  @Override
  public CaseStudyRepository getParentRepository() {
    return caseRepo;
  }

  @Override
  public ContraindicationRepository getRepository() {
    return cdRepository;
  }

  @Override
  public BiPredicate<CaseStudy, Contraindication> getPaternityTesting() {
    return (p, c) -> Objects.equals(p, c.getCaseStudy());
  }

  @Override
  public Function<CaseStudy, RestfulRoute<Long>> getRoute() {
    return (caseStudy) -> new RestfulRoute<Long>() {

      @Override
      public String getIndexPath() {
        return caseStudy.joinPath(CONTRAINDICATION);
      }

    };
  }

  @Override
  public Iterable<Contraindication> getChildren(CaseStudy parent) {
    return caseStudyService.getSortedContraindications(parent);
  }

}
