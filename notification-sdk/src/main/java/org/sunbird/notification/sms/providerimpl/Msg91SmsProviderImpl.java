package org.sunbird.notification.sms.providerimpl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jboss.logging.Logger;
import org.sunbird.notification.sms.Sms;
import org.sunbird.notification.sms.provider.ISmsProvider;
import org.sunbird.notification.utils.JsonUtil;
import org.sunbird.notification.utils.PropertiesCache;
import org.sunbird.notification.utils.SMSFactory;

import com.google.firebase.messaging.Message;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;

public class Msg91SmsProviderImpl implements ISmsProvider {

  private static Logger logger = Logger.getLogger(Msg91SmsProviderImpl.class);

  private static String baseUrl = null;
  private static String postUrl = null;
  private static String sender = null;
  private static String smsRoute = null;
  private static String authKey = null;
  private static String country = null;
  private static Map<String,String> headers = new HashMap<String,String>();
  static {
    boolean resposne = init();
    logger.info("SMS configuration values are set ==" + resposne);
    headers.put("authkey", authKey);
    headers.put("content-type", "application/json");
  }

  /** this method will do the SMS properties initialization. */
  public static boolean init() {
    baseUrl = PropertiesCache.getInstance().getProperty("sunbird.msg.91.baseurl");
    postUrl = PropertiesCache.getInstance().getProperty("sunbird.msg.91.post.url");
    sender = System.getenv("sunbird_msg_sender");
    if (JsonUtil.isStringNullOREmpty(sender)) {
      sender = PropertiesCache.getInstance().getProperty("sunbird.msg.91.sender");
    }
    smsRoute = PropertiesCache.getInstance().getProperty("sunbird.msg.91.route");
    country = PropertiesCache.getInstance().getProperty("sunbird.msg.91.country");
   // authKey = System.getenv("sunbird_msg_91_auth");
    if (JsonUtil.isStringNullOREmpty(authKey)) {
      authKey = PropertiesCache.getInstance().getProperty("sunbird.msg.91.auth");
    }
    return validateSettings();
  }

  @Override
  public boolean sendSms(String phoneNumber, String smsText) {
      return sendMsg(phoneNumber, smsText,null);
    }

  @Override
  public boolean sendSms(String phoneNumber, String countryCode, String smsText) {
      return sendMsg(phoneNumber, smsText,countryCode);
    }

  
  
  
  
	private boolean sendAsyncMsg(String phone, String smsText, String countryCode) {
		List<String> mobileNumbers = new ArrayList<>();
		mobileNumbers.add(phone);
		Sms sms = null; 
		try {
			sms = new Sms(URLEncoder.encode(smsText, "UTF-8"), mobileNumbers);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		List<Sms> smsList = new ArrayList<>();
		smsList.add(sms);
		if (countryCode == null || countryCode.trim().length() == 0) {
			countryCode = country;
		}
		// create body
		ProviderDetails providerDetails = new ProviderDetails(sender, smsRoute, country, smsList);

		String providerDetailsString = JsonUtil.toJson(providerDetails);

		Unirest.post(baseUrl+postUrl).headers(headers).body(providerDetailsString).asJsonAsync(new Callback<JsonNode>() {
			@Override
			public void failed(UnirestException e) {
				logger.error("Msg91SmsProviderImpl:sendAsyncMsg  exception " + e);
			}

			@Override
			public void completed(HttpResponse<JsonNode> response) {
				logger.info("Msg91SmsProviderImpl:sendAsyncMsg send sms response " + response.getStatus() + "--"
						+ response.getBody());
				try {
					Unirest.shutdown();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void cancelled() {
				logger.info("Msg91SmsProviderImpl:sendAsyncMsg send sms cancelled ");
			}
		});

		return true;
	}
  
	
	public static void main(String[] args) {
	    ISmsProvider provider = SMSFactory.getInstance("91SMS");
	    Msg91SmsProviderImpl impl = new Msg91SmsProviderImpl();
	    impl.sendAsyncMsg("9663890445", "Test sms", "91");
	}
  
  /**
   * This method will send SMS using Post method
   *
   * @param mobileNumber String
   * @param countryCode String
   * @param smsText String
   * @return boolean
   */
  private boolean sendMsg(String mobileNumber, String smsText,String countryCode) {
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
        if(countryCode == null || countryCode.trim().length()==0) {
        	countryCode = country;	
        }
        ProviderDetails providerDetails = new ProviderDetails(sender, smsRoute, countryCode, smsList);

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
  private static boolean validateSettings() {
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
public boolean sendOtp(String phoneNumber, String message, String countryCode) {
	try {
		Unirest.post(baseUrl+"api/sendotp.php").headers(headers).body("").asString();
	} catch (UnirestException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return false;
}

@Override
public boolean resendOtp(String phoneNumber, String countryCode) {
	// TODO Auto-generated method stub
	return false;
}

@Override
public boolean verifyOtp(String phoneNumber, String otp, String countryCode) {
	// TODO Auto-generated method stub
	return false;
}
}
