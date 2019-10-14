package org.sunbird.notification.email.service;

import org.sunbird.notification.beans.EmailRequest;

public interface IEmailService {

  /**
   * @param emailReq EmailRequest
   * @return boolean
   */
  public boolean sendEmail(EmailRequest emailReq);
}
