package org.sunbird.notification.email.service.impl;

import org.sunbird.notification.beans.EmailConfig;
import org.sunbird.notification.email.service.IEmailFactory;
import org.sunbird.notification.email.service.IEmailService;

public class IEmailProviderFactory implements IEmailFactory {
  private IEmailService emailservice;

  @Override
  public IEmailService create(EmailConfig config) {
    if (emailservice == null) {
      emailservice = new SmtpEMailServiceImpl(config);
    }
    return emailservice;
  }
}
