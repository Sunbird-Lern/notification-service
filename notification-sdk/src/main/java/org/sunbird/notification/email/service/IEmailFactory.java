package org.sunbird.notification.email.service;

import org.sunbird.notification.beans.EmailConfig;

public interface IEmailFactory {

  IEmailService create(EmailConfig config);
}
