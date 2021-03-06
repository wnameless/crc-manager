package com.wmw.crc.manager.repository;

import java.util.List;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import com.wmw.crc.manager.model.CaseStudy;
import com.wmw.crc.manager.model.Contraindication;

@JaversSpringDataAuditable
@Repository
public interface ContraindicationRepository
    extends JpaRepository<Contraindication, Long>,
    QuerydslPredicateExecutor<Contraindication> {

  List<Contraindication> findAllByCaseStudy(CaseStudy caseStudy);

  List<Contraindication> findAllByCaseStudy(CaseStudy caseStudy, Sort sort);

  List<Contraindication> findAllByCaseStudyAndBundleAndPhrase(CaseStudy cs,
      Integer bundle, String phrase);

  default boolean existsByCaseStudyAndBundleAndPhraseAndTakekinds(CaseStudy cs,
      Integer bundle, String phrase, List<String> takekinds) {
    List<Contraindication> ctdcts =
        findAllByCaseStudyAndBundleAndPhrase(cs, bundle, phrase);
    for (Contraindication ctdct : ctdcts) {
      if (ctdct.getTakekinds().containsAll(takekinds)) {
        return true;
      }
    }

    return false;
  }

}
