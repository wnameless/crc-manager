package com.wmw.crc.manager.repository;

import java.util.List;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.Contraindication;

@JaversSpringDataAuditable
@Repository
public interface ContraindicationRepository
    extends JpaRepository<Contraindication, Long> {

  List<Contraindication> findAllByCaseStudy(CaseStudy caseStudy);

  List<Contraindication> findAllByCaseStudy(CaseStudy caseStudy, Sort sort);

}
