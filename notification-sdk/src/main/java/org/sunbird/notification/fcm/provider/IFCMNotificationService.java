package org.sunbird.notification.fcm.provider;

import java.util.List;
import java.util.Map;
import org.sunbird.notification.utils.FCMResponse;

/**
 * This interface will handle all call regarding FCM notification
 *
 * @author manzarul
 */
public interface IFCMNotificationService {

  /**
   * Method used for sending notification to single device.
   *
   * @param deviceId user device id.
   * @param data notification data
   * @param isDryRun required true for dry run case
   * @return FCMResponse
   */
  public FCMResponse sendSingleDeviceNotification(
      String deviceId, Map<String, String> data, boolean isDryRun);

  /**
   * This api will be used for sending notification to multiple device. max 100 device notification
   * is supported here.
   *
   * @param deviceIds list of device ids
   * @param isDryRun required true for dry run case
   * @param data notification data
   * @return FCMResponse
   */
  public FCMResponse sendMultiDeviceNotification(
      List<String> deviceIds, Map<String, String> data, boolean isDryRun);

  /**
   * Method used for sending topic based notification
   *
   * @param topic name if topic
   * @param data notification data
   * @param isDryRun required true for dry run case
   * @return FCMResponse
   */
  public FCMResponse sendTopicNotification(
      String topic, Map<String, String> data, boolean isDryRun);
}
