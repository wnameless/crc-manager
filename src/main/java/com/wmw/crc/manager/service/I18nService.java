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

  public String caseManagerAdded(Object[] args, Locale locale) {
    return messageSource.getMessage("ctrl.case.operation.message.manager-added",
        args, locale);
  }

  public String caseManagerRemoved(Object[] args, Locale locale) {
    return messageSource.getMessage(
        "ctrl.case.operation.message.manager-removed", args, locale);
  }

  public String caseEditorAdded(Object[] args, Locale locale) {
    return messageSource.getMessage("ctrl.case.operation.message.editor-added",
        args, locale);
  }

  public String caseEditorRemoved(Object[] args, Locale locale) {
    return messageSource
        .getMessage("ctrl.case.operation.message.editor-removed", args, locale);
  }

  public String caseViewerAdded(Object[] args, Locale locale) {
    return messageSource.getMessage("ctrl.case.operation.message.viewer-added",
        args, locale);
  }

  public String caseViewerRemoved(Object[] args, Locale locale) {
    return messageSource
        .getMessage("ctrl.case.operation.message.viewer-removed", args, locale);
  }

  public String subjectNationalIDExisted(Object[] args, Locale locale) {
    return messageSource.getMessage("ctrl.subject.message.nationalid-existed",
        new Object[] {}, locale);
  }

  public String subjectDropoutDateCannotClear(Object[] args, Locale locale) {
    return messageSource.getMessage("ctrl.subject.message.dropout-cannot-clear",
        new Object[] {}, locale);
  }

}
