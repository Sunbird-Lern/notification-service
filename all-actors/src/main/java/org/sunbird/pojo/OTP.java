package org.sunbird.pojo;

public class OTP {

  private int length;

  private int expiryInMinute;

  public OTP() {}

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public int getExpiryInMinute() {
    return expiryInMinute;
  }

  public void setExpiryInMinute(int expiryInMinute) {
    this.expiryInMinute = expiryInMinute;
  }
}
