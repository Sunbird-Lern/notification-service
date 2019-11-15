package org.sunbird.notification.sms.providerimpl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sunbird.notification.beans.MessageResponse;
import org.sunbird.notification.beans.OTPRequest;
import org.sunbird.notification.sms.Sms;
import org.sunbird.notification.sms.provider.ISmsProvider;
import org.sunbird.notification.utils.JsonUtil;
import org.sunbird.notification.utils.NotificationConstant;
import org.sunbird.notification.utils.Util;

/** @author manzarul */
public class Msg91SmsProviderImpl implements ISmsProvider {

  private static Logger logger = LogManager.getLogger(Msg91SmsProviderImpl.class);

  private static String baseUrl = null;
  private static String postUrl = null;
  private String sender = null;
  private static String smsRoute = null;
  private String authKey = null;
  private static String country = null;
  private static Map<String, String> headers = new HashMap<String, String>();
  private static final String OTP_BASE_URL = "https://control.msg91.com/api/";
  ObjectMapper mapper = new ObjectMapper();

  /**
   * @param authKey
   * @param sender
   */
  public Msg91SmsProviderImpl(String authKey, String sender) {
    this.authKey = authKey;
    this.sender = sender;
    boolean resposne = init();
    logger.info("SMS configuration values are set ==" + resposne);
  }

  /** this method will do the SMS properties initialization. */
  public boolean init() {
    baseUrl = Util.readValue(NotificationConstant.SUNBIRD_MSG_91_BASEURL);
    postUrl = Util.readValue(NotificationConstant.SUNBIRD_MSG_91_POST_URL);
    if (StringUtils.isBlank(sender)) {
      sender = Util.readValue(NotificationConstant.SUNBIR_MSG_DEFAULT_SENDER);
    }
    smsRoute = Util.readValue(NotificationConstant.SUNBIRD_MSG_91_ROUTE);
    country = Util.readValue(NotificationConstant.SUNBIRD_DEFAULT_COUNTRY_CODE);
    if (StringUtils.isBlank(authKey)) {
      authKey = Util.readValue(NotificationConstant.SUNBIRD_MSG_91_AUTH);
    }
    headers.put("authkey", authKey);
    headers.put("content-type", "application/json");
    return validateSettings();
  }

  @Override
  public boolean sendSms(String phoneNumber, String smsText) {
    return sendMsg(phoneNumber, smsText, null);
  }

  @Override
  public boolean sendSms(String phoneNumber, String countryCode, String smsText) {
    return sendMsg(phoneNumber, smsText, countryCode);
  }

  /**
   * This method will send SMS using Post method
   *
   * @param mobileNumber String
   * @param countryCode String
   * @param smsText String
   * @return boolean
   */
  private boolean sendMsg(String mobileNumber, String smsText, String countryCode) {
    logger.debug("Msg91SmsProvider@Sending " + smsText + "  to mobileNumber " + mobileNumber);
    CloseableHttpClient httpClient = null;
    try {
      httpClient = HttpClients.createDefault();

      String path = null;

      if (validateSettings(mobileNumber, smsText)) {
        String tempMobileNumber = removePlusFromMobileNumber(mobileNumber);

        logger.debug("Msg91SmsProvider - after removePlusFromMobileNumber " + tempMobileNumber);
        path = baseUrl + postUrl;
        logger.debug("Msg91SmsProvider -Executing request - " + path);

        HttpPost httpPost = new HttpPost(path);

        // add content-type headers
        httpPost.setHeader("content-type", "application/json");

        // add authkey header
        httpPost.setHeader("authkey", authKey);

        List<String> mobileNumbers = new ArrayList<>();
        mobileNumbers.add(tempMobileNumber);

        // create sms
        Sms sms = new Sms(URLEncoder.encode(smsText, "UTF-8"), mobileNumbers);

        List<Sms> smsList = new ArrayList<>();
        smsList.add(sms);

        // create body
        if (countryCode == null || countryCode.trim().length() == 0) {
          countryCode = country;
        }
        ProviderDetails providerDetails =
            new ProviderDetails(sender, smsRoute, countryCode, smsList);

        String providerDetailsString = JsonUtil.toJson(providerDetails);

        if (!JsonUtil.isStringNullOREmpty(providerDetailsString)) {
          logger.debug("Msg91SmsProvider - Body - " + providerDetailsString);

          HttpEntity entity = new ByteArrayEntity(providerDetailsString.getBytes("UTF-8"));
          httpPost.setEntity(entity);

          CloseableHttpResponse response = httpClient.execute(httpPost);
          StatusLine sl = response.getStatusLine();
          response.close();
          if (sl.getStatusCode() != 200) {
            logger.error(
                "SMS code for "
                    + tempMobileNumber
                    + " could not be sent: "
                    + sl.getStatusCode()
                    + " - "
                    + sl.getReasonPhrase());
          }
          return sl.getStatusCode() == 200;
        } else {
          return false;
        }

      } else {
        logger.debug("Msg91SmsProvider - Some mandatory parameters are empty!");
        return false;
      }
    } catch (IOException e) {
      logger.error(e);
      return false;
    } catch (Exception e) {
      logger.info("Msg91SmsProvider - Error in coverting providerDetails to string!");
      return false;
    } finally {
      closeHttpResource(httpClient);
    }
  }

  /**
   * Removing + symbol from mobile number
   *
   * @param mobileNumber String
   * @return String
   */
  private String removePlusFromMobileNumber(String mobileNumber) {
    logger.debug("Msg91SmsProvider - removePlusFromMobileNumber " + mobileNumber);

    if (mobileNumber.startsWith("+")) {
      return mobileNumber.substring(1);
    }
    return mobileNumber;
  }

  /**
   * This method will close the http resource.
   *
   * @param httpClient
   */
  private void closeHttpResource(CloseableHttpClient httpClient) {
    if (httpClient != null) {
      try {
        httpClient.close();
      } catch (IOException e) {
        logger.error(e);
      }
    }
  }

  /**
   * @param phone
   * @param smsText
   * @return
   */
  private boolean validateSettings(String phone, String smsText) {
    if (!JsonUtil.isStringNullOREmpty(sender)
        && !JsonUtil.isStringNullOREmpty(smsRoute)
        && !JsonUtil.isStringNullOREmpty(phone)
        && !JsonUtil.isStringNullOREmpty(authKey)
        && !JsonUtil.isStringNullOREmpty(country)
        && !JsonUtil.isStringNullOREmpty(smsText)) {
      return true;
    }
    logger.error("SMS value is not configure properly.");
    return false;
  }

  /** @return */
  private boolean validateSettings() {
    if (!JsonUtil.isStringNullOREmpty(sender)
        && !JsonUtil.isStringNullOREmpty(smsRoute)
        && !JsonUtil.isStringNullOREmpty(authKey)
        && !JsonUtil.isStringNullOREmpty(country)) {
      return true;
    }
    logger.error("SMS value is not configure properly.");
    return false;
  }

  @Override
  public boolean bulkSms(List<String> phoneNumber, String smsText) {
    List<String> phoneNumberList = null;
    logger.debug("Msg91SmsProvider@Sending " + smsText + "  to mobileNumber ");
    logger.debug(
        "Msg91SmsProvider@SMS Provider parameters \n"
            + "Gateway - "
            + baseUrl
            + "\n"
            + "authKey - "
            + authKey
            + "\n"
            + "sender - "
            + sender
            + "\n"
            + "country - "
            + country
            + "\n"
            + "smsRoute - "
            + smsRoute
            + "\n");

    if (JsonUtil.isStringNullOREmpty(smsText)) {
      logger.debug("can't sent empty msg.");
      return false;
    }
    phoneNumberList = validatePhoneList(phoneNumber);
    if (phoneNumberList == null || phoneNumberList.isEmpty()) {
      logger.debug("can't sent msg with empty phone list.");
      return false;
    }
    CloseableHttpClient httpClient = null;
    try {
      httpClient = HttpClients.createDefault();

      String path = null;
      logger.debug("Inside POST");

      path = baseUrl + postUrl;
      logger.debug("Msg91SmsProvider -Executing request - " + path);

      HttpPost httpPost = new HttpPost(path);

      // add content-type headers
      httpPost.setHeader("content-type", "application/json");

      // add authkey header
      httpPost.setHeader("authkey", authKey);
      // create sms
      Sms sms = new Sms(URLEncoder.encode(smsText, "UTF-8"), phoneNumberList);

      List<Sms> smsList = new ArrayList<>();
      smsList.add(sms);

      // create body
      ProviderDetails providerDetails = new ProviderDetails(sender, smsRoute, country, smsList);

      String providerDetailsString = JsonUtil.toJson(providerDetails);

      if (!JsonUtil.isStringNullOREmpty(providerDetailsString)) {
        logger.debug("Msg91SmsProvider - Body - " + providerDetailsString);

        HttpEntity entity = new ByteArrayEntity(providerDetailsString.getBytes("UTF-8"));
        httpPost.setEntity(entity);

        CloseableHttpResponse response = httpClient.execute(httpPost);
        StatusLine sl = response.getStatusLine();
        response.close();
        if (sl.getStatusCode() != 200) {
          logger.error(
              "SMS code for "
                  + phoneNumberList
                  + " could not be sent: "
                  + sl.getStatusCode()
                  + " - "
                  + sl.getReasonPhrase());
        }
        return sl.getStatusCode() == 200;
      } else {
        return false;
      }

    } catch (IOException e) {
      logger.error(e);
      return false;
    } catch (Exception e) {
      logger.error("Msg91SmsProvider : send : error in converting providerDetails to String");
      return false;
    } finally {
      closeHttpResource(httpClient);
    }
  }

  /**
   * This method will verify list of phone numbers. if any phone number is empty or null then will
   * remove it form list.
   *
   * @param phones List<String>
   * @return List<String>
   */
  private List<String> validatePhoneList(List<String> phones) {
    if (phones != null) {
      Iterator<String> itr = phones.iterator();
      while (itr.hasNext()) {
        String phone = itr.next();
        if (JsonUtil.isStringNullOREmpty(phone) || phone.length() < 10) {
          itr.remove();
        }
      }
    }
    return phones;
  }

  @Override
  public boolean sendOtp(OTPRequest request) {
    if (!isOtpRequestValid(request)) {
      logger.info("Send opt request is not valid.");
      return false;
    }
    boolean otpResponse = false;
    try {
      String data = createOtpReqData(request);
      HttpResponse<String> response =
          Unirest.get(OTP_BASE_URL + "sendotp.php?authkey=" + authKey + data).asString();
      if (response != null) {
        if (response.getStatus() == NotificationConstant.SUCCESS_CODE) {
          MessageResponse messageResponse = convertMsg91Response(response.getBody());
          if (NotificationConstant.SUCCESS.equalsIgnoreCase(messageResponse.getType())) {
            logger.info("OTP sent succssfully with response data " + response.getBody());
            otpResponse = true;
          }
          logger.info("OTP sent response data " + response.getBody());
        } else {
          logger.info(
              "OTP failed to sent with status code and response data "
                  + response.getStatus()
                  + " "
                  + response.getBody());
        }
      }

    } catch (UnirestException e) {
      logger.error(
          "Msg91SmsProviderImpl:sendOtp  exception occured during otp send :" + e.getMessage());
      e.printStackTrace();
    }
    return otpResponse;
  }

  @Override
  public boolean resendOtp(OTPRequest request) {
    if (!isPhoneNumberValid(request.getPhone())) {
      logger.info("resend otp request is not valid ");
      return false;
    }
    boolean response = false;
    try {
      HttpResponse<String> resendResponse =
          Unirest.get(
                  OTP_BASE_URL
                      + "retryotp.php?retrytype=text&authkey="
                      + authKey
                      + NotificationConstant.Ampersand
                      + NotificationConstant.MOBILE
                      + NotificationConstant.EQUAL
                      + request.getCountryCode()
                      + request.getPhone())
              .header("content-type", "application/x-www-form-urlencoded")
              .asString();

      if (resendResponse != null) {
        if (resendResponse.getStatus() == NotificationConstant.SUCCESS_CODE) {
          logger.info("OTP resent response data " + resendResponse.getBody());
          MessageResponse messageResponse = convertMsg91Response(resendResponse.getBody());
          if (NotificationConstant.SUCCESS.equalsIgnoreCase(messageResponse.getType())) {
            logger.info("OTP resent succssfully with response data " + resendResponse.getBody());
            response = true;
          }
        } else {
          logger.info(
              "OTP resent failed with code and response data "
                  + resendResponse.getStatus()
                  + " -"
                  + resendResponse.getBody());
        }

      } else {
        logger.info("OTP resent failed .");
      }

    } catch (Exception e) {
      logger.error(
          "Msg91SmsProviderImpl:sendOtp  exception occured during otp resend :" + e.getMessage());
    }
    return response;
  }

  @Override
  public boolean verifyOtp(OTPRequest request) {
    if (!isOtpRequestValid(request)) {
      logger.info("Verify Opt request is not valid.");
      return false;
    }
    boolean response = false;
    try {
      HttpResponse<String> resendResponse =
          Unirest.get(
                  OTP_BASE_URL
                      + "verifyRequestOTP.php?authkey="
                      + authKey
                      + NotificationConstant.Ampersand
                      + NotificationConstant.MOBILE
                      + NotificationConstant.EQUAL
                      + request.getCountryCode()
                      + request.getPhone()
                      + NotificationConstant.Ampersand
                      + NotificationConstant.OTP
                      + NotificationConstant.EQUAL
                      + request.getOtp())
              .header("content-type", "application/x-www-form-urlencoded")
              .asString();

      if (resendResponse != null) {
        if (resendResponse.getStatus() == NotificationConstant.SUCCESS_CODE) {
          logger.info("OTP verify response data " + resendResponse.getBody());
          MessageResponse messageResponse = convertMsg91Response(resendResponse.getBody());
          if (NotificationConstant.SUCCESS.equalsIgnoreCase(messageResponse.getType())) {
            logger.info("OTP verify succssfully with response data " + resendResponse.getBody());
            response = true;
          }
        } else {
          logger.info(
              "OTP verification failed with code and response data "
                  + resendResponse.getStatus()
                  + " -"
                  + resendResponse.getBody());
        }

      } else {
        logger.info("OTP verification failed .");
      }

    } catch (Exception e) {
      logger.error(
          "Msg91SmsProviderImpl:sendOtp  exception occured during otp verification :"
              + e.getMessage());
    }
    return response;
  }

  private String createOtpReqData(OTPRequest request) {
    StringBuilder builder = new StringBuilder();
    if (StringUtils.isNotBlank(request.getOtp())) {
      builder.append(
          NotificationConstant.Ampersand
              + NotificationConstant.OTP
              + NotificationConstant.EQUAL
              + request.getOtp());
    } else {
      builder.append(
          NotificationConstant.Ampersand
              + NotificationConstant.SENDER
              + NotificationConstant.EQUAL
              + Util.readValue(NotificationConstant.SUNBIR_MSG_DEFAULT_SENDER));
    }
    try {
      builder.append(
          NotificationConstant.Ampersand
              + NotificationConstant.MESSAGE
              + NotificationConstant.EQUAL
              + URLEncoder.encode(request.getMessage(), "UTF-8")
              + NotificationConstant.Ampersand
              + NotificationConstant.MOBILES
              + NotificationConstant.EQUAL
              + request.getCountryCode()
              + request.getPhone());
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    builder.append(
        NotificationConstant.Ampersand
            + NotificationConstant.OTP_LENGTH
            + NotificationConstant.EQUAL
            + request.getOtpLength()
            + NotificationConstant.Ampersand
            + NotificationConstant.OTP_EXPIRY
            + NotificationConstant.EQUAL
            + request.getExpiryTimeInMinute());
    return builder.toString();
  }

  private MessageResponse convertMsg91Response(String response) {
    MessageResponse messageResponse = new MessageResponse();
    try {
      messageResponse = mapper.readValue(response, MessageResponse.class);
    } catch (JsonParseException e) {
      logger.error("Error occured during response parsing:JsonParseException: " + e.getMessage());
    } catch (JsonMappingException e) {
      logger.error(
          "Error occured during response parsing:JsonMappingException : " + e.getMessage());
    } catch (IOException e) {
      logger.error("Error occured during response parsing:IOException : " + e.getMessage());
    }
    return messageResponse;
  }

  private boolean isOtpRequestValid(OTPRequest request) {
    boolean response = isPhoneNumberValid(request.getPhone());
    if (response) {
      response = isOtpLengthValid(request.getOtpLength());
    }
    return response;
  }

  private boolean isPhoneNumberValid(String phone) {
    if (StringUtils.isBlank(phone)) {
      return false;
    } else {
      if (phone.trim().length() > 12 || phone.trim().length() < 10) {
        return false;
      }
    }
    return true;
  }

  private boolean isOtpLengthValid(int otpLength) {
    if (otpLength > 9 || otpLength < 4) {
      return false;
    }
    return true;
  }
}
