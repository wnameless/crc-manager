/*
 *
 * Copyright 2018 Wei-Ming Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.wmw.crc.manager.service;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.wmw.crc.manager.model.Subject.Status;

@Service("i18n")
public class I18nService {

  @Autowired
  MessageSource messageSource;

  public String msg(String code, Locale locale) {
    return messageSource.getMessage(code, new Object[] {}, locale);
  }

  public String takeKind(String code) {
    Locale locale = LocaleContextHolder.getLocale();
    switch (code) {
      case "11":
        return messageSource.getMessage("takekinds.11", new Object[] {},
            locale);
      case "12":
        return messageSource.getMessage("takekinds.12", new Object[] {},
            locale);
      case "13":
        return messageSource.getMessage("takekinds.13", new Object[] {},
            locale);
      case "21":
        return messageSource.getMessage("takekinds.21", new Object[] {},
            locale);
      case "31":
        return messageSource.getMessage("takekinds.31", new Object[] {},
            locale);
      case "41":
        return messageSource.getMessage("takekinds.41", new Object[] {},
            locale);
      case "51":
        return messageSource.getMessage("takekinds.51", new Object[] {},
            locale);
      default:
        return code;
    }
  }

  public String str(Status status) {
    Locale locale = LocaleContextHolder.getLocale();
    switch (status) {
      case PRESCREENING:
        return messageSource.getMessage("ui.subject.status.prescreening",
            new Object[] {}, locale);
      case SCREENING:
        return messageSource.getMessage("ui.subject.status.screening",
            new Object[] {}, locale);
      case UNQUALIFIED:
        return messageSource.getMessage("ui.subject.status.unqualified",
            new Object[] {}, locale);
      case ONGOING:
        return messageSource.getMessage("ui.subject.status.ongoing",
            new Object[] {}, locale);
      case DROPPED:
        return messageSource.getMessage("ui.subject.status.dropped",
            new Object[] {}, locale);
      case FOLLOWUP:
        return messageSource.getMessage("ui.subject.status.followup",
            new Object[] {}, locale);
      case CLOSED:
        return messageSource.getMessage("ui.subject.status.closed",
            new Object[] {}, locale);
      default:
        return messageSource.getMessage("ui.subject.status.prescreening",
            new Object[] {}, locale);
    }
  }

  public String caseManagerAdded(Locale locale, Object... args) {
    return messageSource.getMessage("ctrl.case.operation.message.manager-added",
        args, locale);
  }

  public String caseManagerRemoved(Locale locale, Object... args) {
    return messageSource.getMessage(
        "ctrl.case.operation.message.manager-removed", args, locale);
  }

  public String caseEditorAdded(Locale locale, Object... args) {
    return messageSource.getMessage("ctrl.case.operation.message.editor-added",
        args, locale);
  }

  public String caseEditorRemoved(Locale locale, Object... args) {
    return messageSource
        .getMessage("ctrl.case.operation.message.editor-removed", args, locale);
  }

  public String caseViewerAdded(Locale locale, Object... args) {
    return messageSource.getMessage("ctrl.case.operation.message.viewer-added",
        args, locale);
  }

  public String caseViewerRemoved(Locale locale, Object... args) {
    return messageSource
        .getMessage("ctrl.case.operation.message.viewer-removed", args, locale);
  }

  public String subjectDateUnselect(Locale locale, Object... args) {
    return messageSource.getMessage("ctrl.subject.message.date-unselect",
        new Object[] {}, locale);
  }

  public String subjectUnselect(Locale locale, Object... args) {
    return messageSource.getMessage("ctrl.subject.message.subject-unselect",
        new Object[] {}, locale);
  }

}
