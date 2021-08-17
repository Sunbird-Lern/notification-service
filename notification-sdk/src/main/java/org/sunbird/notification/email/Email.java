package org.sunbird.notification.email;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.notification.beans.Constants;
import org.sunbird.notification.beans.EmailConfig;
import org.sunbird.notification.utils.Util;
import org.sunbird.request.LoggerUtil;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.*;
import java.util.List;
import java.util.Properties;

/**
 * this api is used to sending mail.
 *
 * @author Manzarul.Haque
 */
public class Email {
  private static LoggerUtil logger = new LoggerUtil(Email.class);
  private static Properties props = null;
  private String host;
  private String port;
  private String userName;
  private String password;
  private String fromEmail;
  private static Email instance;

  private Email() {
    init();
    initProps();
  }

  public static Email getInstance() {
    if (null == instance) {
      synchronized (Email.class){
        if (null == instance) {
          instance = new Email();
        }
      }
    }
    return instance;
  }

  public static Email getInstance(EmailConfig config) {
    if (null == instance) {
      synchronized (Email.class){
        if (null == instance) {
          instance = new Email(config);
        }
      }
    }
    return instance;
  }

  private Email(EmailConfig config) {
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

  private Session getSession() {
      return Session.getInstance(props, new GMailAuthenticator(userName, password));
  }

  private Transport getTransportClient() throws MessagingException {
    Transport transport = getSession().getTransport("smtp");
    transport.connect(host, userName, password);
    return transport;
  }

  private Transport getTransportClient(Session session) throws MessagingException {
      Transport transport = session.getTransport("smtp");
      transport.connect(host, userName, password);
      return transport;
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
    Session session = getSession();
    try {
      MimeMessage message = new MimeMessage(session);
      addRecipient(message, Message.RecipientType.TO, emailList);
      addRecipient(message, Message.RecipientType.CC, ccEmailList);
      setMessageAttribute(message, fromEmail, subject, body);
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
      Session session = getSession();
      MimeMessage message = new MimeMessage(session);
      addRecipient(message, Message.RecipientType.TO, emailList);
      message.setSubject(subject);
      Multipart multipart = createMultipartData(emailBody, filePath);
      setMessageAttribute(message, fromEmail, subject, multipart);
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
      Session session = getSession();
      MimeMessage message = new MimeMessage(session);
      addRecipient(message, Message.RecipientType.BCC, bccList);
      setMessageAttribute(message, fromEmail, subject, body);
      sentStatus = sendEmail(session, message);
    } catch (Exception e) {
      sentStatus = false;
      logger.error("SendMail:sendMail: Exception occurred with message = " + e.getMessage(), e);
    }
    return sentStatus;
  }

  private Multipart createMultipartData(String emailBody, String filePath)
    throws AddressException, MessagingException {
    BodyPart messageBodyPart = new MimeBodyPart();
    messageBodyPart.setContent(emailBody, "text/html; charset=utf-8");
    Multipart multipart = new MimeMultipart();
    multipart.addBodyPart(messageBodyPart);
    DataSource source = new FileDataSource(filePath);
    messageBodyPart = new MimeBodyPart();
    messageBodyPart.setDataHandler(new DataHandler(source));
    messageBodyPart.setFileName(filePath);
    multipart.addBodyPart(messageBodyPart);
    return multipart;
  }

  private void addRecipient(MimeMessage message, RecipientType type, List<String> recipient)
    throws AddressException, MessagingException {
    if (CollectionUtils.isEmpty(recipient)) {
      logger.info("Recipient list is empty or null ");
      return;
    }
    for (String email : recipient) {
      message.addRecipient(type, new InternetAddress(email));
    }
  }

  private void setMessageAttribute(
    MimeMessage message, String fromEmail, String subject, String body)
    throws AddressException, MessagingException {
    message.setFrom(new InternetAddress(fromEmail));
    message.setSubject(subject, "utf-8");
    message.setContent(body, "text/html; charset=utf-8");
  }

  private void setMessageAttribute(
    MimeMessage message, String fromEmail, String subject, Multipart multipart)
    throws AddressException, MessagingException {
    message.setFrom(new InternetAddress(fromEmail));
    message.setSubject(subject, "utf-8");
    message.setContent(multipart, "text/html; charset=utf-8");
  }

  private boolean sendEmail(Session session, MimeMessage message) {
    Transport transport = null;
    boolean response = true;
    try {
      transport = getTransportClient(session);
      transport.sendMessage(message, message.getAllRecipients());
        logger.info(message.getContent().toString());
    } catch (Exception e) {
      logger.error("SendMail:sendMail: Exception occurred with message = " + e.getMessage(), e);
      response = false;
    } finally {
      try {
        transport.close();
      } catch (MessagingException e) {
        logger.error("Exception occurred while closing client.",e);
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