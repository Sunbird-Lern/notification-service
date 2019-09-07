/** */
package org.sunbird.notification.dispatcher.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.notification.dispatcher.INotificationDispatcher;
import org.sunbird.notification.fcm.provider.IFCMNotificationService;
import org.sunbird.notification.fcm.provider.NotificationFactory;

/** @author manzarul */
public class FCMNotificationDispatcher implements INotificationDispatcher {
  private IFCMNotificationService service =
      NotificationFactory.getInstance(NotificationFactory.instanceType.httpClinet.name());
  private static final String IDS = "ids";
  private static final String TOPIC = "topic";
  private static final String RAW_DATA = "rawData";
  private static final String NOTIFICATIONS = "notifications";
  private static final String CONFIG = "config";
  private static ObjectMapper mapper = new ObjectMapper();

  @Override
  /**
   * This map will have key as ids/topic and rawData. ids will have list of device registration ids.
   * topic :it will contains name of fcm topic either ids or topic one key is mandatory. and data
   * will have complete data that need to sent.
   */
  public void dispatch(Map<String, Object> data, boolean isDryRun) {
    List<Map<String, Object>> notificationDataList =
        (List<Map<String, Object>>) data.get(NOTIFICATIONS);
    for (int i = 0; i < notificationDataList.size(); i++) {
      Map<String, Object> innerMap = (Map<String, Object>) notificationDataList.get(i);
      List<String> deviceRegIds = null;
      String topicVal = null;
      if (innerMap.get(IDS) != null) {
        deviceRegIds = (List) innerMap.get(IDS);
      }
      if (deviceRegIds == null || deviceRegIds.size() == 0) {
        Map<String, Object> configMap = (Map<String, Object>) innerMap.get(CONFIG);
        topicVal = (String) configMap.getOrDefault(TOPIC, "");
        if (StringUtils.isBlank(topicVal)) {
          throw new RuntimeException("neither device registration id nore topic found in request");
        }
      }
      try {
        String notificationData = mapper.writeValueAsString(innerMap.get(RAW_DATA));
        Map<String, String> map = new HashMap<String, String>();
        map.put(RAW_DATA, notificationData);
        if (StringUtils.isNotBlank(topicVal)) {
          service.sendTopicNotification(topicVal, map, isDryRun);
        } else {
          service.sendMultiDeviceNotification(deviceRegIds, map, isDryRun);
        }
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }
    }
  }
}
