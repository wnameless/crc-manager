/*
 * Copyright (c) 2018 ReiMed Co. to present.
 * All rights reserved.
 */
package com.wmw.crc.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CrcManagerApp {

  public static void main(String[] args) {
    SpringApplication.run(CrcManagerApp.class, args);
  }
}
