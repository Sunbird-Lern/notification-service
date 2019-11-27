package org.sunbird.notification.sms.provider;

import java.util.List;
import org.sunbird.notification.beans.OTPRequest;

/**
 * This interface will provide all method for sending sms.
 *
 * @author manzarul
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
   * This method will send SMS to list of phone numbers. default country code value will differ
   * based on Installation, for sunbird default is 91
   *
   * @param phoneNumber List<String>
   * @param smsText Sms text
   * @return boolean
   */
  boolean bulkSms(List<String> phoneNumber, String smsText);
  /**
   * This method will send OTP to user phone number.
   *
   * @param OTPRequest otp request object. message should have ##otp##. during otp generation
   *     ##otp## will be replaced with OTP value. countryCode String country code value Ex: 91 for
   *     India
   * @return boolean
   */
  boolean sendOtp(OTPRequest otpRequest);
  /**
   * This method is used for resending otp on phone number only.
   *
   * @param OTPRequest otp request countryCode String country code value Ex: 91 for India
   * @return boolean
   */
  boolean resendOtp(OTPRequest otpRequest);
  /**
   * This method will verify provided otp.
   *
   * @param OTPRequest otp request OTP , it should be number only contryCode String country code
   *     value Ex: 91 for India
   * @return boolean
   */
  boolean verifyOtp(OTPRequest otpRequest);
}
