package com.wmw.crc.manager.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false, of = { "id" })
@Data
@Entity
public class Contraindication {

  @Id
  @GeneratedValue
  Long id;

  String phrase;

  String atcCode;

}
