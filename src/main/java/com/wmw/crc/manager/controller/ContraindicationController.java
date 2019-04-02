package com.wmw.crc.manager.controller;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.Optional;

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
import com.wmw.crc.manager.model.Medicine;
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
    CaseStudy c = caseRepo.getOne(id);

    Ruby.Array.of(c.getContraindications()).sortByǃ(cd -> cd.getBundle());
    model.addAttribute("case", c);
    return "contraindication/index";
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @PostMapping("cases/{id}/contraindications")
  String add(Model model, @PathVariable("id") Long id,
      @RequestParam("medicineId") Long medicineId,
      @RequestParam("bundle") Integer bundle,
      @RequestParam("phrase") String phrase,
      @RequestParam("atcCode") String atcCode) {
    CaseStudy c = caseRepo.getOne(id);

    Optional<Medicine> med = medicineRepo.findById(medicineId);

    if (!isNullOrEmpty(phrase) || !isNullOrEmpty(atcCode)) {
      Contraindication cd = new Contraindication();
      cd.setBundle(bundle);
      cd.setPhrase(phrase);
      cd.setAtcCode(atcCode);
      if (med.isPresent()) {
        cd.setDetail(med.get().getName() + "(" + med.get().getEngName() + ")");
      }
      contraindicationRepo.save(cd);

      c.getContraindications().add(cd);
      caseRepo.save(c);
    }

    Ruby.Array.of(c.getContraindications()).sortByǃ(cd -> cd.getBundle());
    model.addAttribute("case", c);
    return "contraindication/index";
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @GetMapping("cases/{id}/contraindications/{cdId}")
  String remove(Model model, @PathVariable("id") Long id,
      @PathVariable("cdId") Long cdId) {
    CaseStudy c = caseRepo.getOne(id);

    Ruby.Array.of(c.getContraindications())
        .removeIf(cd -> cd.getId().equals(cdId));
    caseRepo.save(c);

    Ruby.Array.of(c.getContraindications()).sortByǃ(cd -> cd.getBundle());
    model.addAttribute("case", c);
    return "contraindication/index";
  }

}
