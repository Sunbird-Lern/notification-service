/** */
package org.sunbird.notification.dispatcher.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sunbird.notification.dispatcher.INotificationDispatcher;
import org.sunbird.notification.email.Email;
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
  private ObjectMapper mapper = new ObjectMapper();
  private String topic = null;
  private Producer<Long, String> producer = null;
  private final int BATCH_SIZE = 100;
  private static FCMNotificationDispatcher instance;

  public static FCMNotificationDispatcher getInstance() {
    if (null == instance) {
      synchronized (FCMNotificationDispatcher.class) {
        if (null == instance) {
          instance = new FCMNotificationDispatcher();
        }
      }
    }
    return instance;
  }

  private FCMNotificationDispatcher() {
    initKafkaClient();
  }

  @Override
  /**
   * This map will have key as ids/topic and rawData. ids will have list of device registration ids.
   * topic :it will contains name of fcm topic either ids or topic one key is mandatory. and data
   * will have complete data that need to sent.
   */
  public FCMResponse dispatch(NotificationRequest notification, boolean isDryRun, boolean isSync) {

    if (isSync) {
      return dispatchSync(notification, isDryRun);
    } else {
      return dispatchAsync(notification);
    }
  }

  private FCMResponse dispatchSync(NotificationRequest notification, boolean isDryRun) {
    org.sunbird.pojo.Config config = null;
    if (notification.getIds() == null || notification.getIds().size() == 0) {
      config = notification.getConfig();
      if (StringUtils.isBlank(config.getTopic())) {
        throw new RuntimeException("neither device registration id nor topic found in request");
      }
    }
    FCMResponse response = null;
    try {
      String notificationData = mapper.writeValueAsString(notification.getRawData());
      Map<String, String> map = new HashMap<String, String>();
      String RAW_DATA = "rawData";
      map.put(RAW_DATA, notificationData);
      if (config != null && StringUtils.isNotBlank(config.getTopic())) {
        response = service.sendTopicNotification(config.getTopic(), map, isDryRun);
      } else {
        if (notification.getIds().size() <= BATCH_SIZE) {
          response = service.sendMultiDeviceNotification(notification.getIds(), map, isDryRun);
        } else {
          // Split into 100 batch
          List<String> tmp = new ArrayList<String>();
          for (int i = 0; i < notification.getIds().size(); i++) {
            tmp.add(notification.getIds().get(i));
            if (tmp.size() == BATCH_SIZE || i == (notification.getIds().size() - 1)) {
              response = service.sendMultiDeviceNotification(tmp, map, isDryRun);
              tmp.clear();
              logger.info("sending message in 100 batch.");
            }
          }
        }
      }

    } catch (JsonProcessingException e) {
      logger.error("Error during fcm notification processing." + e.getMessage());
      e.printStackTrace();
    }
    return response;
  }

  /** Initialises Kafka producer required for dispatching messages on Kafka. */
  private void initKafkaClient() {
    if (producer == null) {
      Config config = ConfigUtil.getConfig();
      String BOOTSTRAP_SERVERS = config.getString(Constant.SUNBIRD_NOTIFICATION_KAFKA_SERVICE_CONFIG);
      topic = config.getString(Constant.SUNBIRD_NOTIFICATION_KAFKA_TOPIC);

      logger.info(
          "FCMNotificationDispatcher:initKafkaClient: Bootstrap servers = "
              + BOOTSTRAP_SERVERS);
      logger.info("FCMNotificationDispatcher:initKafkaClient: topic = " + topic);
      try {
        producer =
            KafkaClient.createProducer(
                BOOTSTRAP_SERVERS, Constant.KAFKA_CLIENT_NOTIFICATION_PRODUCER);
      } catch (Exception e) {
        logger.error("FCMNotificationDispatcher:initKafkaClient: An exception occurred.", e);
      }
    }
  }

  private FCMResponse dispatchAsync(NotificationRequest notification) {
    FCMResponse response = null;
    if (CollectionUtils.isNotEmpty(notification.getIds())) {
      if (notification.getIds().size() <= BATCH_SIZE) {
        String message = getTopicMessage(notification);
        response = writeDataToKafka(message, topic);
        logger.info("device id size is less than Batch size");
      } else {
        List<String> deviceIds = notification.getIds();
        logger.info(
            "device id size is greater than Batch size ");
        List<String> tmp = new ArrayList<String>();
        for (int i = 0; i < deviceIds.size(); i++) {
          tmp.add(deviceIds.get(i));
          if (tmp.size() == BATCH_SIZE || i == deviceIds.size() - 1) {
            notification.setIds(tmp);
            String message = getTopicMessage(notification);
            response = writeDataToKafka(message, topic);
            tmp.clear();
          }
        }
      }
    } else {
      String message = getTopicMessage(notification);
      response = writeDataToKafka(message, topic);
    }

    return response;
  }

  private FCMResponse writeDataToKafka(String message, String topic) {
    FCMResponse response = new FCMResponse();
    ProducerRecord<Long, String> record = new ProducerRecord<>(topic, message);
    if (producer != null) {
      producer.send(record);
      response.setMessage_id(1);
      response.setCanonical_ids(System.currentTimeMillis());
      response.setSuccess(Constant.SUCCESS_CODE);
    } else {
      response.setError(Constant.ERROR_DURING_WRITE_DATA);
      response.setFailure(Constant.FAILURE_CODE);
      logger.info("FCMNotificationDispatcher:writeDataToKafka: Kafka producer is not initialised.");
    }
    return response;
  }

  private String getTopicMessage(NotificationRequest notification) {
    KafkaMessage message = new KafkaMessage();
    Actor actor =
        new Actor(Constant.BROAD_CAST_TOPIC_NOTIFICATION_MESSAGE, Constant.ACTOR_TYPE_VALUE);
    message.setActor(actor);
    Map<String, Object> requestMap = new HashMap<String, Object>();
    String topicMessage = null;
    try {
      requestMap.put(
          Constant.NOTIFICATION,
          (Map<String, Object>) mapper.convertValue(notification, Map.class));
      Map<String, Object> object = new HashMap<String, Object>();
      object.put(Constant.ID, getRequestHashed(requestMap));
      object.put(Constant.TYPE, Constant.TYPE_VALUE);
      message.setObject(object);
      EventData evnetData = new EventData();
      evnetData.setAction(Constant.BROAD_CAST_TOPIC_NOTIFICATION_KEY);
      evnetData.setRequest(requestMap);
      message.setEdata(evnetData);
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
