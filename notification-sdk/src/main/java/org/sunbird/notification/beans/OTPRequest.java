/** */
package org.sunbird.notification.beans;

import org.sunbird.notification.utils.NotificationConstant;
import org.sunbird.notification.utils.Util;

/** @author manzarul */
public class OTPRequest {
  private String phone;
  private int otpLength;
  private int expiryTimeInMinute;
  private String message;
  private String countryCode;
  private String otp;

  public OTPRequest() {}

  public OTPRequest(
      String phone, String countryCode, int otpLength, int expiryTime, String message, String otp) {
    this.phone = phone;
    this.otpLength =
        otpLength > 0
            ? otpLength
            : Integer.parseInt(Util.readValue(NotificationConstant.SUNBIRD_OTP_DEFAULT_LENGHT));
    this.countryCode =
        countryCode != null
            ? countryCode
            : Util.readValue(NotificationConstant.SUNBIRD_DEFAULT_COUNTRY_CODE);
    this.message =
        message != null
            ? message
            : Util.readValue(NotificationConstant.SUNBIRD_OTP_DEFAULT_MESSAGE);
    this.expiryTimeInMinute =
        expiryTime > 0
            ? expiryTime
            : Integer.parseInt(
                Util.readValue(NotificationConstant.SUNBIRD_OTP_DEFAULT_EXPIRY_IN_MINUTE));
    this.otp = otp;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public int getOtpLength() {
    return otpLength;
  }

  public void setOtpLength(int otpLength) {
    this.otpLength = otpLength;
  }

  public int getExpiryTimeInMinute() {
    return expiryTimeInMinute;
  }

  public void setExpiryTimeInMinute(int expiryTimeInMinute) {
    this.expiryTimeInMinute = expiryTimeInMinute;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getOtp() {
    return otp;
  }

  public void setOtp(String otp) {
    this.otp = otp;
  }
}
