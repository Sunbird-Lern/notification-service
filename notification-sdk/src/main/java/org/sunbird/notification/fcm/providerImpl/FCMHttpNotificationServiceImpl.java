/** */
package org.sunbird.notification.fcm.providerImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.sunbird.notification.fcm.provider.IFCMNotificationService;
import org.sunbird.notification.utils.FCMResponse;
import org.sunbird.notification.utils.NotificationConstant;
import org.sunbird.request.LoggerUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This notification service will make http call to send device notification.
 *
 * @author manzarul
 */
public class FCMHttpNotificationServiceImpl implements IFCMNotificationService {
  private static LoggerUtil logger = new LoggerUtil(FCMHttpNotificationServiceImpl.class);

  /** FCM_URL URL of FCM server */
  public static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";
  /** FCM_ACCOUNT_KEY FCM server key. */
  private static String FCM_ACCOUNT_KEY =
      System.getenv(NotificationConstant.SUNBIRD_FCM_ACCOUNT_KEY);

  private static Map<String, String> headerMap = new HashMap<>();
  private static final String TOPIC_SUFFIX = "/topics/";
  static ObjectMapper mapper = new ObjectMapper();

  static {
    headerMap.put(NotificationConstant.AUTHORIZATION, FCM_ACCOUNT_KEY);
    headerMap.put("Content-Type", "application/json");
  }

  @Override
  public FCMResponse sendSingleDeviceNotification(
    String deviceId, Map<String, String> data, boolean isDryRun, Map<String,Object> context) {
    List<String> deviceIds = new ArrayList<String>();
    deviceIds.add(deviceId);
    return sendDeviceNotification(deviceIds, data, FCM_URL, isDryRun, context);
  }

  @Override
  public FCMResponse sendMultiDeviceNotification(
      List<String> deviceIds, Map<String, String> data, boolean isDryRun, Map<String,Object> context) {
    return sendDeviceNotification(deviceIds, data, FCM_URL, isDryRun, context);
  }

  @Override
  public FCMResponse sendTopicNotification(
      String topic, Map<String, String> data, boolean isDryRun, Map<String,Object> context) {
    return sendTopicNotification(topic, data, FCM_URL, isDryRun, context);
  }

  public static void setAccountKey(String key) {
    FCM_ACCOUNT_KEY = key;
    headerMap.put(NotificationConstant.AUTHORIZATION, FCM_ACCOUNT_KEY);
  }

  /**
   * This method will send notification to FCM.
   *
   * @param topic String
   * @param data Map<String, Object>
   * @param url String
   * @return String as Json.{"message_id": 7253391319867149192}
   */
  private static FCMResponse sendTopicNotification(
      String topic, Map<String, String> data, String url, boolean isDryRun, Map<String,Object> context) {
    if (StringUtils.isBlank(FCM_ACCOUNT_KEY) || StringUtils.isBlank(url)) {
      logger.info(context, "FCM account key or URL is not provided===" + FCM_URL);
      return null;
    }
    FCMResponse response = null;
    try {
      JSONObject object1 = new JSONObject(data.get(NotificationConstant.RAW_DATA));
      JSONObject object = new JSONObject();
      object.put(NotificationConstant.DATA, object1);
      object.put(NotificationConstant.DRY_RUN, isDryRun);
      object.put(NotificationConstant.TO, TOPIC_SUFFIX + topic);
      logger.info(object.toString());
      HttpResponse<JsonNode> httpResponse =
          Unirest.post(FCM_URL).headers(headerMap).body(object.toString()).asJson();
      String responsebody = httpResponse.getBody().toString();
      logger.info(context, "FCM Notification response== for topic " + topic + response);
      response = mapper.readValue(responsebody, FCMResponse.class);
    } catch (Exception e) {
      logger.error(context, e.getMessage(), e);
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
  private static FCMResponse sendDeviceNotification(
      List<String> deviceIds, Map<String, String> data, String url, boolean isDryRun, Map<String,Object> context) {
    if (StringUtils.isBlank(FCM_ACCOUNT_KEY) || StringUtils.isBlank(url)) {
      logger.info(context, "FCM account key or URL is not provided===" + FCM_URL);
      return null;
    }
    FCMResponse fcmResponse = null;
    try {
      JSONObject object1 = new JSONObject(data.get(NotificationConstant.RAW_DATA));
      JSONObject object = new JSONObject();
      object.put(NotificationConstant.DATA, object1);
      object.put(NotificationConstant.DRY_RUN, isDryRun);
      object.put(NotificationConstant.REGISTRATION_IDS, deviceIds);
      logger.info(object.toString());

      HttpResponse<JsonNode> httpResponse =
          Unirest.post(FCM_URL).headers(headerMap).body(object).asJson();
      String response = httpResponse.getBody().toString();
      logger.info(context, "FCM Notification response== for device ids " + deviceIds + " " + response);
      fcmResponse = mapper.readValue(response, FCMResponse.class);
    } catch (Exception e) {
      logger.error(context, e.getMessage(), e);
    }
    return fcmResponse;
  }
}
