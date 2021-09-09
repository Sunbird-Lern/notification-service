package org.sunbird.notification.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.collections.MapUtils;
import org.sunbird.JsonKey;
import org.sunbird.common.exception.BaseException;
import org.sunbird.pojo.NotificationType;
import org.sunbird.pojo.NotificationV2Request;
import org.sunbird.request.LoggerUtil;
import org.sunbird.common.response.Response;
import org.sunbird.service.NotificationService;
import org.sunbird.service.NotificationServiceImpl;
import org.sunbird.util.Util;
import org.sunbird.utils.PropertiesCache;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class FeedNotificationHandler implements INotificationHandler{
    private static LoggerUtil logger = new LoggerUtil(FeedNotificationHandler.class);
    NotificationService notificationService = NotificationServiceImpl.getInstance();
    ObjectMapper mapper = new ObjectMapper();
    @Override
    public Response sendNotification(NotificationV2Request notificationRequest, boolean isDryRun, boolean isSync, Map<String,Object> reqContext) throws BaseException, IOException {

        logger.info("FeedNotificationHandler: making call to sendNotifications method");
        Response response = new Response();
        if(null != notificationRequest && CollectionUtils.isNotEmpty(notificationRequest.getIds())){
            Map<String,Object> template = Util.getTemplate(notificationRequest, notificationService, reqContext);
            Map<String,Object> dataTemplate =new HashMap<>();
             dataTemplate.put(JsonKey.VER,template.get(JsonKey.VER));
             dataTemplate.put(JsonKey.TYPE,template.get(JsonKey.TYPE));
             dataTemplate.put(JsonKey.DATA,
                       notificationService.transformTemplate((String)template.get(JsonKey.DATA),(Map<String, Object>) template.get(JsonKey.PARAMS)));
             notificationRequest.getAction().put(JsonKey.TEMPLATE,dataTemplate);
             String notificationCategory = PropertiesCache.getInstance().getProperty(JsonKey.NOTIFICATION_CATEGORY_CONFIG);
             if(notificationCategory.contains((String)notificationRequest.getAction().get(JsonKey.CATEGORY))){
                 //Write data into v1 format
                 Map<String,Object> notification = transformV2toV1Notification(notificationRequest);
                 notificationService.createV1NotificationFeed(notification,reqContext);
             }
             response = notificationService.createNotificationFeed(notificationRequest,reqContext);
        }
        return response;
    }


    public Response sendV1Notification(Map<String, Object> notification, Map<String, Object> reqContext) throws JsonProcessingException {
        logger.info("FeedNotificationHandler: making call to sendV1Notifications method");
        Response response = new Response();
        if(MapUtils.isNotEmpty(notification)){
            NotificationV2Request notificationRequest = transformV1toV2Notification(notification);
            response = notificationService.createNotificationFeed(notificationRequest,reqContext);
            if(null != response){
                response = notificationService.createV1NotificationFeed(notification,reqContext);
            }
        }
        return response;
    }

    private NotificationV2Request transformV1toV2Notification(Map<String,Object> notification) throws JsonProcessingException {
           NotificationV2Request notificationV2Request = new NotificationV2Request();
           notificationV2Request.setIds(Arrays.asList((String)notification.get(JsonKey.USER_ID)));
           notificationV2Request.setPriority((int)notification.get(JsonKey.PRIORITY));
           notificationV2Request.setType(NotificationType.FEED.getValue());
           notification.put(JsonKey.USER_ID,Arrays.asList((String)notification.get(JsonKey.USER_ID)));
           Map<String,Object> actionMap = new HashMap<>();
           Map<String,Object> dataMap = (Map<String, Object>) notification.get(JsonKey.DATA);
           Map<String,Object> actionDataMap = (Map<String, Object>) dataMap.get(JsonKey.ACTION_DATA);
           Map<String,Object> additionalInfo = new HashMap<>();
           Map<String,Object> templateData = new HashMap<>();
           for (Map.Entry<String,Object> itr: actionDataMap.entrySet()) {
              if(JsonKey.TITLE.equals(itr.getKey()) || JsonKey.DESCRIPTION.equals(itr.getKey())){
                  templateData.put(itr.getKey(),itr.getValue());
              }else{
                  additionalInfo.put(itr.getKey(),itr.getValue());
              }
           }
           Map<String,Object> createdBy = new HashMap<>();
           createdBy.put(JsonKey.ID,notification.get(JsonKey.CREATED_BY));
           createdBy.put(JsonKey.TYPE,"system");
           Map<String,Object> template = new HashMap<>();
           template.put(JsonKey.DATA,new ObjectMapper().writeValueAsString(templateData));
           template.put(JsonKey.TYPE,"JSON");
           actionMap.put(JsonKey.CREATED_BY,createdBy);
           actionMap.put(JsonKey.TEMPLATE,template);
           actionMap.put(JsonKey.ADDITIONAL_INFO,additionalInfo);
           actionMap.put(JsonKey.TYPE,actionDataMap.get(JsonKey.ACTION_TYPE));
           actionMap.put(JsonKey.CATEGORY,notification.get(JsonKey.CATEGORY));
           notificationV2Request.setAction(actionMap);
           return notificationV2Request;
    }

    private Map<String,Object> transformV2toV1Notification(NotificationV2Request notificationV2Request) throws IOException {
        Map<String,Object> notification = new HashMap<>();
        notification.put(JsonKey.USER_ID,notificationV2Request.getIds());
        notification.put(JsonKey.PRIORITY,notificationV2Request.getPriority());
        notification.put(JsonKey.CATEGORY,notificationV2Request.getAction().get(JsonKey.CATEGORY));
        notification.put(JsonKey.CREATED_BY, ((Map<String,Object>)notificationV2Request.getAction().get(JsonKey.CREATED_BY)).get(JsonKey.ID));
        Map<String,Object> dataMap = new HashMap<>();
        String templateStr = (String) ((Map<String,Object>)notificationV2Request.getAction().get(JsonKey.TEMPLATE)).get(JsonKey.DATA);
        Map<String,Object> actionDataMap = mapper.readValue(templateStr,Map.class);
        Map<String,Object> additionalInfo = (Map<String,Object>)notificationV2Request.getAction().get(JsonKey.ADDITIONAL_INFO);
        for (Map.Entry<String,Object> itr: additionalInfo.entrySet()) {
            actionDataMap.put(itr.getKey(),itr.getValue());
        }
        dataMap.put(JsonKey.ACTION_DATA,actionDataMap);
        notification.put(JsonKey.DATA,dataMap);
        return notification;
    }

}
