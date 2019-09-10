package org.sunbird.notification.sms.provider;

import java.util.List;

/**
 * This interface will provide all method for sending sms.
 * @author manzarul
 *
 */
public interface ISmsProvider {

  /**
   * This method will send the SMS with default country code. default country code value will differ
   * based on Installation, for sunbird default is 91
   *
   * @param phoneNumber String
   * @param smsText Sms text
   * @return boolean
   */
  boolean sendSms(String phoneNumber, String smsText);

  /**
   * This method will send SMS on user provider country code, basically it will override the value
   * of default country code.
   *
   * @param phoneNumber String
   * @param countryCode
   * @param smsText
   * @return boolean
   */
  boolean sendSms(String phoneNumber, String countryCode, String smsText);

  /**
   * This method will send SMS to list of phone numbers. default country code
   * value will differ based on Installation, for sunbird default is 91
   *
   * @param phoneNumber List<String>
   * @param smsText Sms text
   * @return boolean
   */
  boolean bulkSms(List<String> phoneNumber, String smsText);
  /**
   * This method will send OTP to user phone number.
   * @param phoneNumber phone number without country code.
   * @param message message should have ##otp##. during otp generation 
   * ##otp## will be replaced with OTP value.
   * @param countryCode String country code value Ex: 91 for India
   * @return boolean
   */
  boolean sendOtp (String phoneNumber, String message,String countryCode);
  /**
   * This method is used for resending otp on phone number only.
   * @param phoneNumber phone number with out country code
   * @param countryCode String country code value Ex: 91 for India
   * @return boolean
   */
  boolean resendOtp (String phoneNumber,String countryCode); 
  /**
   * This method will verify provided otp.
   * @param phoneNumber phone number with out country code
   * @param otp OTP , it should be number only
   * @param countryCode String country code value Ex: 91 for India
   * @return boolean
   */
  boolean verifyOtp (String phoneNumber, String otp,String countryCode);
}
