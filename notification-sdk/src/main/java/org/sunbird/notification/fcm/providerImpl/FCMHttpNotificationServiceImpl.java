/** */
package org.sunbird.notification.fcm.providerImpl;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.sunbird.notification.fcm.provider.IFCMNotificationService;
import org.sunbird.notification.utils.NotificationConstant;
import org.sunbird.notification.utils.PropertiesCache;

/**
 * This notification service will make http call to send device notification.
 *
 * @author manzarul
 */
public class FCMHttpNotificationServiceImpl implements IFCMNotificationService {
  private static Logger logger = Logger.getLogger("FCMHttpNotificationServiceImpl");

  /** FCM_URL URL of FCM server */
  public static final String FCM_URL =
      PropertiesCache.getInstance().getProperty(NotificationConstant.FCM_URL);
  /** FCM_ACCOUNT_KEY FCM server key. */
  private static final String FCM_ACCOUNT_KEY =
      System.getenv(NotificationConstant.SUNBIRD_FCM_ACCOUNT_KEY);

  private static Map<String, String> headerMap = new HashMap<>();
  private static final String TOPIC_SUFFIX = "/topics/";

  static {
    headerMap.put(NotificationConstant.AUTHORIZATION, FCM_ACCOUNT_KEY);
    headerMap.put("Content-Type", "application/json");
  }

  @Override
  public String sendSingleDeviceNotification(
      String deviceId, Map<String, String> data, boolean isDryRun) {
    List<String> deviceIds = new ArrayList<String>();
    deviceIds.add(deviceId);
    return sendDeviceNotification(deviceIds, data, FCM_URL, isDryRun);
  }

  @Override
  public List<String> sendMultiDeviceNotification(
      List<String> deviceIds, Map<String, String> data, boolean isDryRun) {
    String response = sendDeviceNotification(deviceIds, data, FCM_URL, isDryRun);
    List<String> list = new ArrayList<String>();
    list.add(response);
    return list;
  }

  @Override
  public String sendTopicNotification(String topic, Map<String, String> data, boolean isDryRun) {
    return sendTopicNotification(topic, data, FCM_URL, isDryRun);
  }

  /**
   * This method will send notification to FCM.
   *
   * @param topic String
   * @param data Map<String, Object>
   * @param url String
   * @return String as Json.{"message_id": 7253391319867149192}
   */
  private static String sendTopicNotification(
      String topic, Map<String, String> data, String url, boolean isDryRun) {
    if (StringUtils.isBlank(FCM_ACCOUNT_KEY) || StringUtils.isBlank(url)) {
      logger.info("FCM account key or URL is not provided===" + FCM_URL);
      return NotificationConstant.FAILURE;
    }
    String response = null;
    try {
      JSONObject object1 = new JSONObject(data);
      JSONObject object = new JSONObject();
      object.put(NotificationConstant.DATA, object1);
      object.put(NotificationConstant.DRY_RUN, isDryRun);
      object.put(NotificationConstant.TO, TOPIC_SUFFIX + topic);
      HttpResponse<JsonNode> httpResponse =
          Unirest.post(FCM_URL).headers(headerMap).body(object.toString()).asJson();
      response = httpResponse.getBody().toString();
      logger.info("FCM Notification response== for topic " + topic + response);
      object1 = null;
      object1 = new JSONObject(response);
      long val = object1.getLong(NotificationConstant.MESSAGE_Id);
      response = val + "";
    } catch (Exception e) {
      response = NotificationConstant.FAILURE;
      logger.info(e.getMessage());
    }
    return response;
  }

  /**
   * This method will send notification to FCM.
   *
   * @param deviceIds list of string
   * @param data Map<String, Object>
   * @param url String
   * @return String as Json.{"message_id": 7253391319867149192}
   */
  private static String sendDeviceNotification(
      List<String> deviceIds, Map<String, String> data, String url, boolean isDryRun) {
    if (StringUtils.isBlank(FCM_ACCOUNT_KEY) || StringUtils.isBlank(url)) {
      logger.info("FCM account key or URL is not provided===" + FCM_URL);
      return NotificationConstant.FAILURE;
    }
    String response = null;
    try {
      JSONObject object1 = new JSONObject(data);
      JSONObject object = new JSONObject();
      object.put(NotificationConstant.DATA, object1);
      object.put(NotificationConstant.DRY_RUN, isDryRun);
      object.put(NotificationConstant.REGISTRATION_IDS, deviceIds);
      HttpResponse<String> httpResponse =
          Unirest.post(FCM_URL).headers(headerMap).body(object.toString()).asString();
      response = httpResponse.getBody().toString();
      logger.info("FCM Notification response== for device ids " + deviceIds + " " + response);
      object1 = null;
      object1 = new JSONObject(response);
      long val = object1.getLong(NotificationConstant.MESSAGE_Id);
      response = val + "";
    } catch (Exception e) {
      response = NotificationConstant.FAILURE;
      logger.info(e.getMessage());
    }
    return response;
  }

  public static void main(String[] args) {
    Map<String, String> data = new HashMap<>();
    data.put("test", "test app notification");
    IFCMNotificationService ifcmNotificationService = new FCMHttpNotificationServiceImpl();
    ifcmNotificationService.sendSingleDeviceNotification(
        "1548a271a9532297712ff65c17fc8ca6", data, true);
  }
}
