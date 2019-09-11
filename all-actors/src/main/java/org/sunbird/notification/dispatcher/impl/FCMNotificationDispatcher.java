/** */
package org.sunbird.notification.dispatcher.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sunbird.notification.dispatcher.INotificationDispatcher;
import org.sunbird.notification.fcm.provider.IFCMNotificationService;
import org.sunbird.notification.fcm.provider.NotificationFactory;
import org.sunbird.notification.utils.FCMResponse;
import org.sunbird.pojo.*;
import org.sunbird.pojo.KafkaMessage;
import org.sunbird.util.ConfigUtil;
import org.sunbird.util.Constant;
import org.sunbird.util.DataHash;
import org.sunbird.util.kafka.KafkaClient;

/** @author manzarul */
public class FCMNotificationDispatcher implements INotificationDispatcher {
  private static Logger logger = LogManager.getLogger(FCMNotificationDispatcher.class);
  private IFCMNotificationService service =
      NotificationFactory.getInstance(NotificationFactory.instanceType.httpClinet.name());
  private static final String IDS = "ids";
  private static final String TOPIC = "topic";
  private static final String RAW_DATA = "rawData";
  private static final String NOTIFICATIONS = "notifications";
  private static final String CONFIG = "config";
  private static final String SUNBIRD_NOTIFICATION_DEFAULT_DISPATCH_MODE =
      "sunbird_notification_default_dispatch_mode";
  private static final String SUNBIRD_NOTIFICATION_DEFAULT_DISPATCH_MODE_VAL = "async";
  private static ObjectMapper mapper = new ObjectMapper();
  String topic = null;
  String BOOTSTRAP_SERVERS = null;
  Producer<Long, String> producer = null;

  @Override
  /**
   * This map will have key as ids/topic and rawData. ids will have list of device registration ids.
   * topic :it will contains name of fcm topic either ids or topic one key is mandatory. and data
   * will have complete data that need to sent.
   */
  public List<FCMResponse> dispatch(Map<String, Object> data, boolean isDryRun) {

    if (System.getenv(SUNBIRD_NOTIFICATION_DEFAULT_DISPATCH_MODE) != null
        && !SUNBIRD_NOTIFICATION_DEFAULT_DISPATCH_MODE_VAL.equalsIgnoreCase(
            System.getenv(SUNBIRD_NOTIFICATION_DEFAULT_DISPATCH_MODE))) {
      return dispatchSync(data, isDryRun);
    } else {
      return dispatchAsync(data);
    }
  }

  private List<FCMResponse> dispatchSync(Map<String, Object> data, boolean isDryRun) {
    List<Map<String, Object>> notificationDataList =
        (List<Map<String, Object>>) data.get(NOTIFICATIONS);
    List<FCMResponse> dispatchResponse = new ArrayList<>();
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
      FCMResponse response = null;
      try {
        String notificationData = mapper.writeValueAsString(innerMap.get(RAW_DATA));
        Map<String, String> map = new HashMap<String, String>();
        map.put(RAW_DATA, notificationData);
        if (StringUtils.isNotBlank(topicVal)) {
          response = service.sendTopicNotification(topicVal, map, isDryRun);
        } else {
          response = service.sendMultiDeviceNotification(deviceRegIds, map, isDryRun);
        }
        dispatchResponse.add(response);

      } catch (JsonProcessingException e) {
        logger.error("Error during fcm notification processing." + e.getMessage());
        e.printStackTrace();
      }
    }
    return dispatchResponse;
  }

  /** Initialises Kafka producer required for dispatching messages on Kafka. */
  private void initKafkaClient() {
    if (producer == null) {
      Config config = ConfigUtil.getConfig();
      BOOTSTRAP_SERVERS = config.getString(Constant.SUNBIRD_NOTIFICATION_KAFKA_SERVICE_CONFIG);
      topic = config.getString(Constant.SUNBIRD_NOTIFICATION_KAFKA_TOPIC);

      logger.info(
          "KafkaTelemetryDispatcherActor:initKafkaClient: Bootstrap servers = "
              + BOOTSTRAP_SERVERS);
      logger.info("UserMergeActor:initKafkaClient: topic = " + topic);
      try {
        producer =
            KafkaClient.createProducer(
                BOOTSTRAP_SERVERS, Constant.KAFKA_CLIENT_NOTIFICATION_PRODUCER);
      } catch (Exception e) {
        logger.error("UserMergeActor:initKafkaClient: An exception occurred.", e);
      }
    }
  }

  private List<FCMResponse> dispatchAsync(Map<String, Object> data) {
    List<FCMResponse> dispatchResponse = new ArrayList<>();
    initKafkaClient();
    List<Map<String, Object>> notificationDataList =
        (List<Map<String, Object>>) data.get(NOTIFICATIONS);
    for (int i = 0; i < notificationDataList.size(); i++) {
      FCMResponse response = new FCMResponse();
      Map<String, Object> innerMap = (Map<String, Object>) notificationDataList.get(i);
      String message = getTopicMessage(innerMap);
      ProducerRecord<Long, String> record = new ProducerRecord<>(topic, message);
      if (producer != null) {
        producer.send(record);
        response.setMessage_id(i + 1);
      } else {
        response.setError("Data write error to kafka topic");
        response.setFailure(500);
        logger.info("UserMergeActor:mergeCertCourseDetails: Kafka producer is not initialised.");
      }
      dispatchResponse.add(response);
    }
    return dispatchResponse;
  }

  private String getTopicMessage(Map<String, Object> data) {
    KafkaMessage message = new KafkaMessage();
    Actor actor =
        new Actor(Constant.BROAD_CAST_TOPIC_NOTIFICATION_MESSAGE, Constant.ACTOR_TYPE_VALUE);
    message.setActor(actor);
    Map<String, Object> requestMap = new HashMap<String, Object>();
    requestMap.put(Constant.NOTIFICATION, data);
    Map<String, Object> object = new HashMap<String, Object>();
    object.put(Constant.ID, getRequestHashed(requestMap));
    object.put(Constant.TYPE, Constant.TYPE_VALUE);
    message.setObject(object);
    EventData evnetData = new EventData();
    evnetData.setAction(Constant.BROAD_CAST_TOPIC_NOTIFICATION_KEY);
    evnetData.setRequest(requestMap);
    message.setEdata(evnetData);
    String topicMessage = null;
    try {
      topicMessage = mapper.writeValueAsString(message);
    } catch (JsonProcessingException e) {
      logger.error("Error occured during data parsing==" + e.getMessage());
      e.printStackTrace();
    }
    return topicMessage;
  }

  private String getRequestHashed(Map<String, Object> request) {
    String val = null;
    try {
      val = DataHash.getHashed(mapper.writeValueAsString(request));
    } catch (Exception e) {
      logger.error("exception occured during hash of request data:" + e.getMessage());
    }
    return val;
  }
}
