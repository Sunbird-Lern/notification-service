package org.sunbird.notification.handler;

import org.sunbird.common.exception.BaseException;
import org.sunbird.common.response.Response;
import org.sunbird.pojo.NotificationV2Request;

import java.io.IOException;
import java.util.Map;


public interface INotificationHandler {

    Response sendNotification(NotificationV2Request notificationRequestList, boolean isDryRun, boolean isSync, Map<String,Object> reqContext) throws BaseException, IOException;

}
