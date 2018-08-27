package com.wmw.crc.manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wmw.crc.manager.model.Medicine;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {}
