package org.sunbird.notification.fcm.provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sunbird.notification.fcm.providerImpl.FCMHttpNotificationServiceImpl;
import org.sunbird.notification.fcm.providerImpl.FCMNotificationServiceImpl;

public class NotificationFactory {
  private static Logger logger = LogManager.getLogger(NotificationFactory.class);

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
