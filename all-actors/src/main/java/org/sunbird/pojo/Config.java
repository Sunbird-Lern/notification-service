package org.sunbird.pojo;

public class Config {

  private String sender;

  private String topic;

  private OTP otp;

  private String subject;

  public Config() {}

  public String getSender() {
    return sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public OTP getOtp() {
    return otp;
  }

  public void setOtp(OTP otp) {
    this.otp = otp;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }
}
