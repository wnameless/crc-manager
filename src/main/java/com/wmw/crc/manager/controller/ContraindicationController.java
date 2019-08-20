package com.wmw.crc.manager.controller;

import static com.google.common.base.Strings.isNullOrEmpty;

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
    CaseStudy cs = caseRepo.getOne(id);

    // System.err.println("HAHA");
    // List<CaseStudy> cases = caseRepo.findByStatus(Status.EXEC);
    // for (CaseStudy c : cases) {
    // RubyHash<Integer, RubyArray<Contraindication>> bundles = Ruby.Array
    // .of(c.getContraindications()).groupBy(Contraindication::getBundle);
    //
    // for (Subject s : c.getSubjects()) {
    // PatientContraindication pc = new PatientContraindication();
    // pc.setNationalId(s.getNationalId());
    // pc.setIrbName(c.getTrialName());
    // pc.setIrbNumber(c.getIrbNumber());
    // pc.setPatientId(s.getNationalId());
    // pc.setStartDate(LocalDate.parse(c.getExpectedStartDate())
    // .format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    // pc.setEndDate(LocalDate.parse(c.getExpectedEndDate())
    // .format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    //
    // if (bundles.containsKey(s.getContraindicationBundle())) {
    // RubyArray<Contraindication> cds =
    // bundles.get(s.getContraindicationBundle());
    // for (Contraindication cd : cds) {
    // List<Medicine> meds = medicineRepo
    // .findByNameContainsOrEngNameContainsOrScientificNameContainsAllIgnoreCase(
    // cd.getPhrase(), cd.getPhrase(), cd.getPhrase());
    //
    // for (Medicine med : meds) {
    // if (cd.getTakekinds().contains(med.getTakekind())) {
    // SimpleDrug sd = new SimpleDrug();
    // sd.setPhrase(cd.getPhrase());
    // sd.setAtcCode(med.getAtcCode1());
    // sd.setHospitalCode(med.getHospitalCode());
    // sd.setMemo(cd.getMemo());
    // pc.getDrugs().add(sd);
    // }
    // }
    // }
    // }
    //
    // System.err.println(pc);
    // }
    // }

    Ruby.Array.of(cs.getContraindications()).sortByǃ(cd -> cd.getBundle());
    model.addAttribute("case", cs);
    return "contraindication/index";
  }

  @PreAuthorize("@perm.canWrite(#id)")
  @PostMapping("cases/{id}/contraindications")
  String add(Model model, @PathVariable("id") Long id,
      @RequestParam("bundle") Integer bundle,
      @RequestParam("phrase") String phrase,
      @RequestParam("takekinds") List<String> takekinds,
      @RequestParam("memo") String memo) {
    CaseStudy cs = caseRepo.getOne(id);

    if (!isNullOrEmpty(phrase)) {
      Contraindication cd = new Contraindication();
      cd.setBundle(bundle);
      cd.setPhrase(phrase);
      cd.setTakekinds(takekinds);
      cd.setMemo(memo);
      contraindicationRepo.save(cd);

      cs.getContraindications().add(cd);
      caseRepo.save(cs);
    }

    Ruby.Array.of(cs.getContraindications()).sortByǃ(cd -> cd.getBundle());
    model.addAttribute("case", cs);
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
