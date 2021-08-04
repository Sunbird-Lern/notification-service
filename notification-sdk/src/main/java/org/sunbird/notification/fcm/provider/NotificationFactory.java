package org.sunbird.notification.fcm.provider;

import org.sunbird.notification.fcm.providerImpl.FCMHttpNotificationServiceImpl;
import org.sunbird.notification.fcm.providerImpl.FCMNotificationServiceImpl;
import org.sunbird.request.LoggerUtil;

public class NotificationFactory {
  private static LoggerUtil logger = new LoggerUtil(NotificationFactory.class);

  public enum instanceType {
    adminClient(),
    httpClinet();
  }

  private NotificationFactory() {}

  public static IFCMNotificationService getInstance(String instance) {
    if (instanceType.adminClient.name().equalsIgnoreCase(instance)) {
      return getAdminInstance();
    } else if (instanceType.httpClinet.name().equalsIgnoreCase(instance)) {
      return getHttpInstance();
    } else {
      logger.info("provided method parameter is not valid " + instance);
      return null;
    }
  }

  private static IFCMNotificationService getAdminInstance() {
    return new FCMNotificationServiceImpl();
  }

  private static IFCMNotificationService getHttpInstance() {
    return new FCMHttpNotificationServiceImpl();
  }
}
