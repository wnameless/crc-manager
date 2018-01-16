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

import static com.google.common.collect.Sets.newHashSet;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.wmw.crc.manager.account.model.User;
import com.wmw.crc.manager.account.repository.RoleRepository;
import com.wmw.crc.manager.account.repository.UserRepository;

@Controller
public class UserController {

  @Autowired
  UserRepository userRepo;

  @Autowired
  RoleRepository roleRepo;

  @Autowired
  Validator validator;

  @RequestMapping(path = "/users/new", method = RequestMethod.GET)
  String registration(Model model) {
    model.addAttribute("userForm", new User());
    return "users/new";
  }

  @RequestMapping(path = "/users", method = RequestMethod.POST)
  String registration(@ModelAttribute("userForm") User userForm,
      @RequestBody MultiValueMap<String, String> formData,
      BindingResult bindingResult, Model model) {
    Map<String, String> errors = new LinkedHashMap<>();

    if (!Objects.equals(formData.get("email"), formData.get("emailConfirm"))) {
      errors.put("email", "2 input emails are not same");
    }

    if (!Objects.equals(formData.get("password"),
        formData.get("passwordConfirm"))) {
      errors.put("password", "2 input passwords are not same");
    }

    validator.validate(userForm, bindingResult);

    for (ObjectError err : bindingResult.getAllErrors()) {
      FieldError fErr = (FieldError) err;
      errors.put(fErr.getField(), fErr.getDefaultMessage());
    }

    if (!errors.isEmpty()) {
      model.addAttribute("errors", errors);
      model.addAttribute("userForm", userForm);
      return "user/new";
    }

    try {
      userForm.setRoles(newHashSet(roleRepo.findByName("ROLE_USER")));
      userRepo.save(userForm);
    } catch (Exception e) {
      if (e instanceof ConstraintViolationException) {
        for (ConstraintViolation<?> cv : ((ConstraintViolationException) e)
            .getConstraintViolations()) {
          errors.put(cv.getPropertyPath().toString(), cv.getMessage());
        }
        model.addAttribute("errors", errors);
      }

      if (e instanceof DataIntegrityViolationException) {
        DataIntegrityViolationException dive =
            (DataIntegrityViolationException) e;
        errors.put("username", dive.getLocalizedMessage());
        model.addAttribute("errors", errors);
      }

      model.addAttribute("userForm", userForm);

      return "user/new";
    }

    return "redirect:login";
  }

}