/*
 *
 * Copyright 2018 Wei-Ming Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.wmw.crc.manager.account.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wmw.crc.manager.account.model.Role;
import com.wmw.crc.manager.account.repository.RoleRepository;

@Controller
public class RoleController {

  @Autowired
  RoleRepository roleRepo;

  @RequestMapping(value = "/role/{id}", method = RequestMethod.GET)
  String role(Model model, @PathVariable("id") Long id) {
    model.addAttribute("role", roleRepo.getOne(id));
    return "role/role";
  }

  @RequestMapping(value = "/roles", method = RequestMethod.GET)
  String roles(Model model) {
    model.addAttribute("roles", roleRepo.findAll());
    return "role/roles";
  }

  @RequestMapping(value = "role/add", method = RequestMethod.GET)
  String registration(Model model) {
    model.addAttribute("roleForm", new Role());

    return "role/add";
  }

  @RequestMapping(value = "role/add", method = RequestMethod.POST)
  String add(@ModelAttribute("roleForm") Role roleForm,
      BindingResult bindingResult, Model model) {
    if (bindingResult.hasErrors()) {
      return "role/add";
    }

    roleRepo.save(roleForm);

    return "redirect:/roles";
  }

  @RequestMapping(value = "role/delete", method = RequestMethod.POST)
  @ResponseBody
  DeleteResData delete(@RequestBody DeleteReqData data) {
    // TODO: implement delete
    return new DeleteResData(data.id, "200", "not implement yet!");
  }
}

class DeleteReqData {
  public int id;
}

class DeleteResData {

  public int id;
  public String code;
  public String msg;

  public DeleteResData(int id, String code, String msg) {
    this.id = id;
    this.code = code;
    this.msg = msg;
  }

}