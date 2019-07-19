package com.wmw.crc.manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wmw.crc.manager.model.Contraindication;

//@JaversSpringDataAuditable
@Repository
public interface ContraindicationRepository
    extends JpaRepository<Contraindication, Long> {}
