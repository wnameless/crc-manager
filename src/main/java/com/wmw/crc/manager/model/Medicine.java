package com.wmw.crc.manager.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false, of = { "id" })
@Data
@Entity
public class Medicine {

  @Id
  @GeneratedValue
  Long id;

  String name;

  String engName;

  String hospitalCode;

  String atcCode1;

  String atcCode2;

  String atcCode3;

  String atcCode4;

}
