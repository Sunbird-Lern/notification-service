package org.sunbird.notification.handler;

import org.apache.commons.collections.CollectionUtils;

import org.sunbird.JsonKey;
import org.sunbird.common.exception.BaseException;
import org.sunbird.pojo.NotificationV2Request;
import org.sunbird.request.LoggerUtil;
import org.sunbird.common.response.Response;
import org.sunbird.service.NotificationService;
import org.sunbird.service.NotificationServiceImpl;
import org.sunbird.util.Util;

import java.util.HashMap;
import java.util.Map;


public class FeedNotificationHandler implements INotificationHandler{
    private static LoggerUtil logger = new LoggerUtil(FeedNotificationHandler.class);
    NotificationService notificationService = NotificationServiceImpl.getInstance();
    @Override
    public Response sendNotification(NotificationV2Request notificationRequest, boolean isDryRun, boolean isSync, Map<String,Object> reqContext) throws BaseException {

        logger.info("FeedNotificationHandler: making call to sendNotifications method");
        Response response = new Response();
        if(null != notificationRequest && CollectionUtils.isNotEmpty(notificationRequest.getIds())){
            Map<String,Object> template = Util.getTemplate(notificationRequest, notificationService, reqContext);
            Map<String,Object> dataTemplate =new HashMap<>();
             dataTemplate.put(JsonKey.VER,template.get(JsonKey.VER));
             dataTemplate.put(JsonKey.TYPE,template.get(JsonKey.TYPE));
             dataTemplate.put(JsonKey.DATA,
                       notificationService.transformTemplate((String)template.get(JsonKey.TEMPLATE),(Map<String, Object>) template.get(JsonKey.PARAMS)));
             notificationRequest.getAction().setTemplate(dataTemplate);
             response = notificationService.createNotificationFeed(notificationRequest,reqContext);
        }
        return response;
    }


}
