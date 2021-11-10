package org.sunbird.notification.handler;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.sunbird.JsonKey;
import org.sunbird.common.exception.BaseException;
import org.sunbird.notification.dispatcher.NotificationRouter;
import org.sunbird.notification.dispatcher.NotificationRouter.DeliveryMode;

import org.sunbird.notification.dispatcher.SyncMessageDispatcher;

import org.sunbird.pojo.*;
import org.sunbird.request.LoggerUtil;
import org.sunbird.common.response.Response;
import org.sunbird.service.NotificationService;
import org.sunbird.service.NotificationServiceImpl;
import org.sunbird.util.Util;

import java.util.HashMap;
import java.util.Map;


public class EmailNotificationHandler implements INotificationHandler{
    private static LoggerUtil logger = new LoggerUtil(EmailNotificationHandler.class);
    private NotificationService notificationService = NotificationServiceImpl.getInstance();
    private SyncMessageDispatcher syDispatcher = new SyncMessageDispatcher();
    ObjectMapper mapper = new ObjectMapper();

    @Override
    public Response sendNotification(NotificationV2Request notificationRequest, boolean isDryRun, boolean isSync, Map<String,Object> reqContext) throws BaseException {
        logger.info("EmailNotificationHandler: making call to sendNotifications method");
        Response response = new Response();
        if(null != notificationRequest && CollectionUtils.isNotEmpty(notificationRequest.getIds())){
            Map<String, Object> responseMap = new HashMap<String, Object>();
            Map<String,Object> template = Util.getTemplate(notificationRequest, notificationService, reqContext);
            Map<String,Object> dataTemplate =new HashMap<>();
            dataTemplate.put(JsonKey.VER,template.get(JsonKey.VER));
            dataTemplate.put(JsonKey.TYPE,template.get(JsonKey.TYPE));
            dataTemplate.put(JsonKey.DATA,
                    notificationService.transformTemplate((String)template.get(JsonKey.DATA),(Map<String, Object>) template.get(JsonKey.PARAMS)));
            dataTemplate.put(JsonKey.PARAMS,template.get(JsonKey.PARAMS));
            notificationRequest.getAction().put(JsonKey.TEMPLATE,dataTemplate);
            NotificationRequest notification = createNotificationObj(notificationRequest, (Map<String, Object>) template.get(JsonKey.CONFIG));
            if (isSync) {
                response = syDispatcher.syncDispatch(notification, reqContext);
            } else {
                response = Util.writeDataToKafka(notification, response, isDryRun, responseMap, isSync, reqContext);
            }
        }
        return response;
    }

    private NotificationRequest createNotificationObj(NotificationV2Request notificationRequest, Map<String,Object> templateConfig) {

        NotificationRequest notification = new NotificationRequest();
        notification.setIds(notificationRequest.getIds());
        notification.setMode(DeliveryMode.email.name());
        Config config = new Config();
        config.setSubject((String) templateConfig.get(JsonKey.SUBJECT));
        config.setSender((String) templateConfig.get(JsonKey.SENDER));
        notification.setConfig(config);
        Template template = new Template();
        template.setData((String) ((Map<String,Object>)notificationRequest.getAction().get(JsonKey.TEMPLATE)).get(JsonKey.DATA));
        JsonNode jsonNode = mapper.convertValue((Map<String,Object>)((Map<String,Object>)notificationRequest.getAction().get(JsonKey.TEMPLATE)).get(JsonKey.PARAMS),JsonNode.class);
        template.setParams(jsonNode);
        notification.setTemplate(template);
        notification.setDeliveryType(NotificationRouter.DeliveryType.message.name());
        return notification;
    }
}
