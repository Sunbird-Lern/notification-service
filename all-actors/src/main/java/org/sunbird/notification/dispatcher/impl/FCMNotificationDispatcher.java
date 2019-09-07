/** */
package org.sunbird.notification.dispatcher.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
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
  private static final String DATA = "data";
  private static ObjectMapper mapper = new ObjectMapper();

  @Override
  /**
   * This map will have key as ids/topic and rawData. ids will have list of device registration ids.
   * topic :it will contains name of fcm topic either ids or topic one key is mandatory. and data
   * will have complete data that need to sent.
   */
  public void dispatch(Map<String, Object> data, boolean isDryRun) {
    String topicVal = (String) data.getOrDefault(TOPIC, "");
    List<String> deviceRegIds = null;
    if (StringUtils.isBlank(topicVal)) {
      deviceRegIds = (List) data.getOrDefault(IDS, new ArrayList<String>());
      if (deviceRegIds == null || deviceRegIds.size() == 0) {
        throw new RuntimeException("neither device registration id nore topic found in request");
      }
    }
    try {
      String notificationData = mapper.writeValueAsString(data.get(DATA));
      Map<String, String> map = new HashMap<String, String>();
      map.put(DATA, notificationData);
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
