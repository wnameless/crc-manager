package com.wmw.crc.manager.controller;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.List;
import java.util.Objects;

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
import com.wmw.crc.manager.repository.ContraindicationRepository;
import com.wmw.crc.manager.repository.MedicineRepository;
import com.wmw.crc.manager.repository.SubjectRepository;

import net.sf.rubycollect4j.Ruby;

@Controller
public class ContraindicationController {

  @Autowired
  CaseStudyRepository caseRepo;

  @Autowired
  SubjectRepository subjectRepo;

  @Autowired
  MedicineRepository medicineRepo;

  @Autowired
  ContraindicationRepository contraindicationRepo;

  @PreAuthorize("@perm.canRead(#id)")
  @GetMapping("/cases/{id}/contraindications")
  String index(Model model, @PathVariable("id") Long id) {
    CaseStudy cs = caseRepo.findById(id).get();
    List<Contraindication> cds = contraindicationRepo.findAllByCaseStudy(cs);
    Ruby.Array.of(cds).sortByǃ(cd -> cd.getBundle());

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

    if (!isNullOrEmpty(phrase)) {
      Contraindication cd = new Contraindication();
      cd.setCaseStudy(cs);
      cd.setBundle(bundle);
      cd.setPhrase(phrase);
      cd.setTakekinds(takekinds);
      cd.setMemo(memo);
      contraindicationRepo.save(cd);
    }

    List<Contraindication> cds = contraindicationRepo.findAllByCaseStudy(cs);
    Ruby.Array.of(cds).sortByǃ(cd -> cd.getBundle());

    model.addAttribute("case", cs);
    model.addAttribute("contraindications", cds);
    return "contraindication/index";
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @GetMapping("cases/{id}/contraindications/{cdId}")
  String remove(Model model, @PathVariable("id") Long id,
      @PathVariable("cdId") Long cdId) {
    CaseStudy cs = caseRepo.findById(id).get();
    List<Contraindication> cds = contraindicationRepo.findAllByCaseStudy(cs);
    Ruby.Array.of(cds).sortByǃ(cd -> cd.getBundle());

    Contraindication target =
        Ruby.Array.of(cds).find(cd -> Objects.equals(cdId, cd.getId()));

    if (target != null) {
      contraindicationRepo.delete(target);
    }

    cds = contraindicationRepo.findAllByCaseStudy(cs);
    Ruby.Array.of(cds).sortByǃ(cd -> cd.getBundle());

    model.addAttribute("case", cs);
    model.addAttribute("contraindications", cds);
    return "contraindication/index";
  }

}
