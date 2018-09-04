package com.wmw.crc.manager.controller;

import static com.google.common.base.Strings.isNullOrEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.wmw.crc.manager.model.Case;
import com.wmw.crc.manager.model.Contraindication;
import com.wmw.crc.manager.repository.CaseRepository;
import com.wmw.crc.manager.repository.ContraindicationRepository;
import com.wmw.crc.manager.repository.MedicineRepository;

import net.sf.rubycollect4j.Ruby;

@Controller
public class ContraindicationController {

  @Autowired
  CaseRepository caseRepo;

  @Autowired
  MedicineRepository medicineRepo;

  @Autowired
  ContraindicationRepository contraindicationRepo;

  @PreAuthorize("@perm.canRead(#id)")
  @GetMapping("/cases/{id}/contraindications")
  String index(Model model, @PathVariable("id") Long id) {
    Case c = caseRepo.findOne(id);

    model.addAttribute("case", c);
    return "contraindication/index";
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @PostMapping("cases/{id}/contraindications")
  String add(Model model, @PathVariable("id") Long id,
      @RequestParam("phrase") String phrase,
      @RequestParam("atcCode") String atcCode) {
    Case c = caseRepo.findOne(id);

    if (!isNullOrEmpty(phrase) || !isNullOrEmpty(atcCode)) {
      Contraindication cd = new Contraindication();
      cd.setPhrase(phrase);
      cd.setAtcCode(atcCode);
      contraindicationRepo.save(cd);

      c.getContraindications().add(cd);
      caseRepo.save(c);
    }

    model.addAttribute("case", c);
    return "contraindication/index";
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @GetMapping("cases/{id}/contraindications/{cdId}")
  String remove(Model model, @PathVariable("id") Long id,
      @PathVariable("cdId") Long cdId) {
    Case c = caseRepo.findOne(id);

    Ruby.Array.of(c.getContraindications())
        .removeIf(cd -> cd.getId().equals(cdId));
    caseRepo.save(c);

    model.addAttribute("case", c);
    return "contraindication/index";
  }

}
