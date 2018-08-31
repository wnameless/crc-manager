package com.wmw.crc.manager;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.wmw.crc.manager.model.Medicine;
import com.wmw.crc.manager.repository.MedicineRepository;

@Profile("prod")
@Component
public class DBInitializer {

  @Autowired
  MedicineRepository medicineRepo;

  @PostConstruct
  void init() {
    if (medicineRepo.count() == 0) {
      Medicine m1 = new Medicine();
      m1.setName("普拿疼");
      m1.setEngName("Acetaminophen");
      m1.setAtcCode1("N02BE01");
      medicineRepo.save(m1);
    }
  }

}
