/** */
package org.sunbird.notification.email.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sunbird.notification.beans.EmailConfig;
import org.sunbird.notification.beans.EmailRequest;
import org.sunbird.notification.email.service.IEmailService;
import org.sunbird.notification.utils.email.SendMail;

/** @author manzarul */
public class SmtpEMailServiceImpl implements IEmailService {
  private static Logger logger = LogManager.getLogger(SmtpEMailServiceImpl.class);
  private SendMail sendEmail = null;

  public SmtpEMailServiceImpl() {
    sendEmail = new SendMail();
  }

  public SmtpEMailServiceImpl(EmailConfig config) {
    sendEmail = new SendMail(config);
  }

  @Override
  public boolean sendEmail(EmailRequest emailReq) {
    if (emailReq == null) {
      logger.info("Email request is null or empty:");
      return false;
    } else if (CollectionUtils.isNotEmpty(emailReq.getBcc())) {
      return sendEmail.sendEmail(
          emailReq.getTo().get(0), emailReq.getSubject(), emailReq.getBody(), emailReq.getBcc());
    } else if (CollectionUtils.isNotEmpty(emailReq.getCc())) {
      return sendEmail.sendMail(
          emailReq.getTo(), emailReq.getSubject(), emailReq.getBody(), emailReq.getCc());
    } else {
      return sendEmail.sendMail(emailReq.getTo(), emailReq.getSubject(), emailReq.getBody());
    }
  }
}
