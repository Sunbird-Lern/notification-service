package org.sunbird.notification.beans;

import java.util.List;
import java.util.Map;

public class EmailRequest {

  private String subject;
  private List<String> to;
  private List<String> cc;
  private List<String> bcc;
  private String templateName;
  private String body;
  private Map<String, String> param;

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public List<String> getTo() {
    return to;
  }

  public void setTo(List<String> to) {
    this.to = to;
  }

  public List<String> getCc() {
    return cc;
  }

  public void setCc(List<String> cc) {
    this.cc = cc;
  }

  public List<String> getBcc() {
    return bcc;
  }

  public void setBcc(List<String> bcc) {
    this.bcc = bcc;
  }

  public String getTemplateName() {
    return templateName;
  }

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public Map<String, String> getParam() {
    return param;
  }

  public void setParam(Map<String, String> param) {
    this.param = param;
  }

  public EmailRequest(
      String subject,
      List<String> to,
      List<String> cc,
      List<String> bcc,
      String templateName,
      String body,
      Map<String, String> param) {
    super();
    this.subject = subject;
    this.to = to;
    this.cc = cc;
    this.bcc = bcc;
    this.templateName = templateName;
    this.body = body;
    this.param = param;
  }

  public EmailRequest() {
    super();
  }
}
