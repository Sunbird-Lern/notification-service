package org.sunbird.notification.email.service;

import org.sunbird.notification.beans.EmailRequest;
import org.sunbird.request.RequestContext;

public interface IEmailService {

  /**
   * @param emailReq EmailRequest
   * @return boolean
   */
  public boolean sendEmail(EmailRequest emailReq, RequestContext context);
}
