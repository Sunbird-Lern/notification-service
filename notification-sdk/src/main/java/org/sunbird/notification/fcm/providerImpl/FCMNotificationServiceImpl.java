package org.sunbird.notification.fcm.providerImpl;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.sunbird.notification.fcm.provider.FCMInitializer;
import org.sunbird.notification.fcm.provider.IFCMNotificationService;
import org.sunbird.notification.utils.FCMResponse;
import org.sunbird.request.LoggerUtil;
import org.sunbird.request.RequestContext;

public class FCMNotificationServiceImpl implements IFCMNotificationService {
  private static LoggerUtil logger = new LoggerUtil(FCMNotificationServiceImpl.class);

  @Override
  public FCMResponse sendSingleDeviceNotification(
      String deviceId, Map<String, String> data, boolean isDryRun, RequestContext context) {
    logger.info(context, "sendSinfleDeviceNotification method started.");
    Message message = Message.builder().putAllData(data).setToken(deviceId).build();
    logger.info(context, "Message going to be sent:" + message);
    String response = null;
    try {
      response = FCMInitializer.getInstance().send(message, isDryRun);
      logger.info(context, "Response from FCM :" + response);
    } catch (FirebaseMessagingException e) {
      logger.error(context,"Exception occured during notification sent: " + e.getMessage(),e);
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public FCMResponse sendMultiDeviceNotification(
      List<String> deviceIds, Map<String, String> data, boolean isDryRun, RequestContext context) {
    List<String> responseDetails = new ArrayList<String>();
    if (deviceIds == null || deviceIds.size() == 0 || deviceIds.size() > 100) {
      throw new RuntimeException(
          "Either device id list is zero or greater than 100. Supported max size is 100.");
    }
    MulticastMessage message =
        MulticastMessage.builder().putAllData(data).addAllTokens(deviceIds).build();
    BatchResponse responses = null;
    try {
      responses = FCMInitializer.getInstance().sendMulticast(message, isDryRun);
    } catch (FirebaseMessagingException e) {
      logger.error(context, "exception occured==" + e.getMessage(), e);
      throw new RuntimeException("FCM Server error");
    }
    List<SendResponse> responseList = responses.getResponses();
    for (SendResponse response : responseList) {
      responseDetails.add(response.getMessageId());
    }
    return null;
  }

  @Override
  public FCMResponse sendTopicNotification(
    String topic, Map<String, String> data, boolean isDryRun, RequestContext context) {
    Message message = Message.builder().putAllData(data).setTopic(topic).build();
    logger.info(context, "Message going to be sent:" + message);
    String response = null;
    try {
      response = FCMInitializer.getInstance().send(message, isDryRun);
      logger.info(context, "Response from FCM :" + response);
    } catch (FirebaseMessagingException e) {
      logger.error(context,"Exception occured during notification sent: " + e.getMessage(), e);
      e.printStackTrace();
    }
    return null;
  }
}
