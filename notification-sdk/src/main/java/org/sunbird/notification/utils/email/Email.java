package org.sunbird.notification.utils.email;

import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sunbird.notification.beans.Constants;
import org.sunbird.notification.beans.EmailConfig;
import org.sunbird.notification.utils.Util;

/**
 * this api is used to sending mail.
 *
 * @author Manzarul.Haque
 */
public class Email {
  private static Logger logger = LogManager.getLogger(Email.class);
  private static Properties props = null;
  private String host;
  private String port;
  private String userName;
  private String password;
  private String fromEmail;

  public Email() {
    init();
    initProps();
  }

  public Email(EmailConfig config) {
    this.fromEmail =
        StringUtils.isNotBlank(config.getFromEmail())
            ? config.getFromEmail()
            : Util.readValue(Constants.EMAIL_SERVER_FROM);
    this.userName =
        StringUtils.isNotBlank(config.getUserName())
            ? config.getUserName()
            : Util.readValue(Constants.EMAIL_SERVER_USERNAME);
    this.password =
        StringUtils.isNotBlank(config.getPassword())
            ? config.getPassword()
            : Util.readValue(Constants.EMAIL_SERVER_PASSWORD);

    this.host =
        StringUtils.isNotBlank(config.getHost())
            ? config.getHost()
            : Util.readValue(Constants.EMAIL_SERVER_HOST);
    this.port =
        StringUtils.isNotBlank(config.getPort())
            ? config.getPort()
            : Util.readValue(Constants.EMAIL_SERVER_PORT);
    initProps();
  }

  private boolean init() {
    boolean response = true;
    host = Util.readValue(Constants.EMAIL_SERVER_HOST);
    port = Util.readValue(Constants.EMAIL_SERVER_PORT);
    userName = Util.readValue(Constants.EMAIL_SERVER_USERNAME);
    password = Util.readValue(Constants.EMAIL_SERVER_PASSWORD);
    fromEmail = Util.readValue(Constants.EMAIL_SERVER_FROM);
    if (StringUtils.isBlank(host)
        || StringUtils.isBlank(port)
        || StringUtils.isBlank(userName)
        || StringUtils.isBlank(password)
        || StringUtils.isBlank(fromEmail)) {
      logger.info(
          "Email setting value is not provided by Env variable=="
              + host
              + " "
              + port
              + " "
              + fromEmail);
      response = false;
    } else {
      logger.info("All email properties are set correctly.");
    }
    return response;
  }

  private void initProps() {
    props = System.getProperties();
    props.put("mail.smtp.host", host);
    props.put("mail.smtp.socketFactory.port", port);
    /*
     * props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
     */
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.port", port);
  }

  /**
   * This method will send email to provided email list.
   *
   * @param emailList List of recipient
   * @param body email body
   * @param subject Subject of email
   */
  public boolean sendMail(List<String> emailList, String subject, String body) {
    return sendMail(emailList, subject, body, null);
  }

  /**
   * Send email (with Cc) using.
   *
   * @param emailList List of recipient emails
   * @param subject email subject
   * @param body email body
   * @param ccEmailList List of Cc emails
   * @return boolean
   */
  public boolean sendMail(
      List<String> emailList, String subject, String body, List<String> ccEmailList) {
    boolean response = true;
    try {
      Session session = Session.getInstance(props, new GMailAuthenticator(userName, password));
      MimeMessage message = new MimeMessage(session);
      message.setFrom(new InternetAddress(fromEmail));
      int size = CollectionUtils.isNotEmpty(emailList) ? emailList.size() : 0;
      int i = 0;
      while (size > 0) {
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailList.get(i)));
        i++;
        size--;
      }
      size = CollectionUtils.isNotEmpty(ccEmailList) ? ccEmailList.size() : 0;
      i = 0;
      while (size > 0) {
        message.addRecipient(Message.RecipientType.CC, new InternetAddress(ccEmailList.get(i)));
        i++;
        size--;
      }
      message.setSubject(subject);
      message.setContent(body, "text/html; charset=utf-8");
      response = sendEmail(session, message);
    } catch (Exception e) {
      response = false;
      logger.error("Exception occured during email sending " + e, e);
    }
    return response;
  }

  /**
   * Send email (with attachment) and given body.
   *
   * @param emailList List of recipient emails
   * @param emailBody Text of email body
   * @param subject Subject of email
   * @param filePath Path of attachment file
   */
  public void sendAttachment(
      List<String> emailList, String emailBody, String subject, String filePath) {
    try {
      Session session = Session.getInstance(props, new GMailAuthenticator(userName, password));
      MimeMessage message = new MimeMessage(session);
      message.setFrom(new InternetAddress(fromEmail));
      int size = emailList.size();
      int i = 0;
      while (size > 0) {
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailList.get(i)));
        i++;
        size--;
      }
      message.setSubject(subject);
      BodyPart messageBodyPart = new MimeBodyPart();
      messageBodyPart.setContent(emailBody, "text/html; charset=utf-8");
      // messageBodyPart.setText(mail);
      // Create a multipar message
      Multipart multipart = new MimeMultipart();
      multipart.addBodyPart(messageBodyPart);
      DataSource source = new FileDataSource(filePath);
      messageBodyPart = null;
      messageBodyPart = new MimeBodyPart();
      messageBodyPart.setDataHandler(new DataHandler(source));
      messageBodyPart.setFileName(filePath);
      multipart.addBodyPart(messageBodyPart);
      message.setSubject(subject);
      message.setContent(multipart);
      sendEmail(session, message);
    } catch (Exception e) {
      logger.error("Exception occured during email sending " + e, e);
    }
  }

  /**
   * This method will send email with bcc.
   *
   * @param fromEmail fromEmail which will come in to.
   * @param subject email subject
   * @param body email body
   * @param bccList recipient bcc list
   * @return boolean
   */
  public boolean sendEmail(String fromEmail, String subject, String body, List<String> bccList) {
    boolean sentStatus = true;
    try {
      Session session = Session.getInstance(props, new GMailAuthenticator(userName, password));
      MimeMessage message = new MimeMessage(session);
      message.setFrom(new InternetAddress(fromEmail));
      RecipientType recipientType = null;
      recipientType = (bccList.size() > 1) ? Message.RecipientType.BCC : Message.RecipientType.TO;
      for (String email : bccList) {
        message.addRecipient(recipientType, new InternetAddress(email));
      }
      message.setSubject(subject);
      message.setContent(body, "text/html; charset=utf-8");
      sentStatus = sendEmail(session, message);
    } catch (Exception e) {
      sentStatus = false;
      logger.error("SendMail:sendMail: Exception occurred with message = " + e.getMessage(), e);
    }
    return sentStatus;
  }

  private boolean sendEmail(Session session, MimeMessage message) {
    Transport transport = null;
    boolean response = true;
    try {
      transport = session.getTransport("smtp");
      transport.connect(host, userName, password);
      transport.sendMessage(message, message.getAllRecipients());
    } catch (Exception e) {
      logger.error("SendMail:sendMail: Exception occurred with message = " + e.getMessage(), e);
      response = false;
    } finally {
      if (transport != null) {
        try {
          transport.close();
        } catch (MessagingException e) {
          logger.error(e.toString(), e);
        }
      }
    }
    return response;
  }

  public String getHost() {
    return host;
  }

  public String getPort() {
    return port;
  }

  public String getUserName() {
    return userName;
  }

  public String getPassword() {
    return password;
  }

  public String getFromEmail() {
    return fromEmail;
  }
}
