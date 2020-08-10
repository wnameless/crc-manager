package com.wmw.crc.manager.controller;

import static com.wmw.crc.manager.RestfulPath.Names.CASE_STUDY;
import static com.wmw.crc.manager.RestfulPath.Names.CONTRAINDICATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.github.wnameless.advancedoptional.AdvOpt;
import com.github.wnameless.spring.common.web.ModelPolicy;
import com.github.wnameless.spring.common.web.NestedRestfulController;
import com.github.wnameless.spring.common.web.RestfulRoute;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.Contraindication;
import com.wmw.crc.manager.model.QContraindication;
import com.wmw.crc.manager.repository.CaseStudyRepository;
import com.wmw.crc.manager.repository.ContraindicationRepository;
import com.wmw.crc.manager.service.CaseStudyService;
import com.wmw.crc.manager.service.I18nService;

@RequestMapping("/" + CASE_STUDY + "/{parentId}/" + CONTRAINDICATION)
@Controller
public class ContraindicationController implements NestedRestfulController< //
    CaseStudy, Long, CaseStudyRepository, //
    Contraindication, Long, ContraindicationRepository> {

  @Autowired
  CaseStudyRepository caseStudyRepo;
  @Autowired
  ContraindicationRepository cdRepository;
  @Autowired
  CaseStudyService caseStudyService;
  @Autowired
  I18nService i18n;

  CaseStudy caseStudy;

  @Override
  public void configure(ModelPolicy<CaseStudy> parentPolicy,
      ModelPolicy<Contraindication> childPolicy,
      ModelPolicy<Iterable<Contraindication>> childrenPolicy) {
    parentPolicy.afterInit(p -> caseStudy = p);
  }

  @PreAuthorize("@perm.canRead(#parentId)")
  @GetMapping
  String index(@PathVariable Long parentId) {
    return "contraindications/index";
  }

  @PreAuthorize("@perm.canWrite(#parentId)")
  @PostMapping
  String create(Model model, @PathVariable Long parentId,
      @RequestParam("bundle") Integer bundle,
      @RequestParam("phrase") String phrase,
      @RequestParam("takekinds") List<String> takekinds,
      @RequestParam("memo") String memo, //
      RedirectAttributes redirectAttrs) {
    AdvOpt<Contraindication> ctdctOpt = caseStudyService
        .addContraindication(caseStudy, bundle, phrase, takekinds, memo);

    if (ctdctOpt.hasMessage()) {
      redirectAttrs.addFlashAttribute("message",
          i18n.msg(ctdctOpt.getMessage()));
    }
    return "redirect:" + caseStudy.joinPath("contraindications");
  }

  @PreAuthorize("@perm.canWrite(#parentId)")
  @DeleteMapping(path = "/{id}", produces = APPLICATION_JSON_VALUE)
  String destroyJS(Model model, @PathVariable Long parentId,
      @PathVariable Long id) {
    caseStudyService.removeContraindication(caseStudy, id);

    updateChildrenByParent(model, caseStudy);
    return "contraindications/list :: partial";
  }

  @Override
  public Function<CaseStudy, RestfulRoute<Long>> getRoute() {
    return (caseStudy) -> RestfulRoute.of(caseStudy.joinPath(CONTRAINDICATION));
  }

  @Override
  public CaseStudyRepository getParentRepository() {
    return caseStudyRepo;
  }

  @Override
  public ContraindicationRepository getChildRepository() {
    return cdRepository;
  }

  @Override
  public BiPredicate<CaseStudy, Contraindication> getPaternityTesting() {
    return (p, c) -> {
      QContraindication qContraindication = QContraindication.contraindication;
      BooleanExpression eqId = qContraindication.id.eq(c.getId());
      BooleanExpression eqCs = qContraindication.caseStudy.eq(p);

      return cdRepository.exists(eqId.and(eqCs));
    };
  }

  @Override
  public Iterable<Contraindication> getChildren(CaseStudy parent) {
    return caseStudyService.getSortedContraindications(parent);
  }

}
