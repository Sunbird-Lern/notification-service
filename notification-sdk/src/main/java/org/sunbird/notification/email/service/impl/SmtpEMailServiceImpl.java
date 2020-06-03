/** */
package org.sunbird.notification.email.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sunbird.notification.beans.EmailConfig;
import org.sunbird.notification.beans.EmailRequest;
import org.sunbird.notification.email.Email;
import org.sunbird.notification.email.service.IEmailService;

/** @author manzarul */
public class SmtpEMailServiceImpl implements IEmailService {
  private static Logger logger = LogManager.getLogger(SmtpEMailServiceImpl.class);
  private Email email = null;

  public SmtpEMailServiceImpl() {
    email = Email.getInstance();
  }

  public SmtpEMailServiceImpl(EmailConfig config) {
    email = Email.getInstance(config);
  }

  @Override
  public boolean sendEmail(EmailRequest emailReq) {
    if (emailReq == null) {
      logger.info("Email request is null or empty:");
      return false;
      // either email object has bcc or to list size more than 1 then pass it as bcc.
    } else if (CollectionUtils.isNotEmpty(emailReq.getBcc()) || emailReq.getTo().size() > 1) {
      return email.sendEmail(
          email.getFromEmail(),
          emailReq.getSubject(),
          emailReq.getBody(),
          CollectionUtils.isEmpty(emailReq.getBcc()) ? emailReq.getTo() : emailReq.getBcc());
    } else if (CollectionUtils.isNotEmpty(emailReq.getCc())) {
      return email.sendMail(
          emailReq.getTo(), emailReq.getSubject(), emailReq.getBody(), emailReq.getCc());
    } else {
      return email.sendMail(emailReq.getTo(), emailReq.getSubject(), emailReq.getBody());
    }
  }
}
