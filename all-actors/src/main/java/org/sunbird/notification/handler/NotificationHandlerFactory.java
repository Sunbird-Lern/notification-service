package org.sunbird.notification.handler;

import org.sunbird.pojo.NotificationType;

public class NotificationHandlerFactory {

    public static INotificationHandler getNotificationHandler(String type){
        INotificationHandler notificationHandler=null;
        if(NotificationType.EMAIL.getValue().equals(type)){
            notificationHandler = new EmailNotificationHandler();
        }else if(NotificationType.PHONE.getValue().equals(type)){
            notificationHandler = new PhoneNotificationHandler();
        }else if(NotificationType.DEVICE.getValue().equals(type)){
            notificationHandler = new DeviceNotificationHandler();
        }else  if(NotificationType.FEED.getValue().equals(type)){
            notificationHandler = new FeedNotificationHandler();
        }
        return notificationHandler;
    }
}
