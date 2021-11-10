package org.sunbird.notification.handler;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.MapUtils;
import org.sunbird.JsonKey;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.notification.dispatcher.NotificationRouter;
import org.sunbird.notification.dispatcher.SyncMessageDispatcher;
import org.sunbird.pojo.Config;
import org.sunbird.pojo.NotificationRequest;
import org.sunbird.pojo.NotificationV2Request;
import org.sunbird.request.LoggerUtil;
import org.sunbird.common.response.Response;
import org.sunbird.service.NotificationService;
import org.sunbird.service.NotificationServiceImpl;
import org.sunbird.util.Util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;


public class DeviceNotificationHandler implements INotificationHandler{
    private static LoggerUtil logger = new LoggerUtil(DeviceNotificationHandler.class);
    ObjectMapper mapper = new ObjectMapper();

    @Override
    public Response sendNotification(NotificationV2Request notificationRequest, boolean isDryRun, boolean isSync, Map<String,Object> reqContext) throws BaseException {
        logger.info("DeviceNotificationHandler: making call to sendNotifications method");
        Response response = new Response();
        if(null != notificationRequest){
            Map<String, Object> responseMap = new HashMap<String, Object>();
            NotificationRequest notification = createNotificationObj(notificationRequest);
            response = Util.writeDataToKafka(notification, response, isDryRun, responseMap, isSync, reqContext);
        }
        return response;
    }

    private NotificationRequest createNotificationObj(NotificationV2Request notificationRequest) throws BaseException {
        NotificationRequest notification = new NotificationRequest();
        notification.setIds(notificationRequest.getIds());
        notification.setMode(NotificationRouter.DeliveryMode.device.name());
        Config config = new Config();
        String topic = ((Map<String,Object>)notificationRequest.getAction().get(JsonKey.ADDITIONAL_INFO)).get(JsonKey.TOPIC) !=null ?
                (String) ((Map<String,Object>)notificationRequest.getAction().get(JsonKey.ADDITIONAL_INFO)).get(JsonKey.TOPIC) : null;
        config.setTopic(topic);
        notification.setConfig(config);
        notification.setDeliveryType(NotificationRouter.DeliveryType.message.name());
        Map<String,Object> rawData = (Map<String, Object>) ((Map<String, Object>)notificationRequest.getAction().get(JsonKey.ADDITIONAL_INFO)).get(JsonKey.RAW_DATA);
        if(MapUtils.isEmpty(rawData)){
            throw new BaseException(IResponseMessage.Key.MANDATORY_PARAMETER_MISSING, MessageFormat.format(IResponseMessage.Message.MANDATORY_PARAMETER_MISSING,JsonKey.RAW_DATA), ResponseCode.CLIENT_ERROR.getCode());
        }
        notification.setRawData(mapper.valueToTree(rawData));
        return notification;
    }
}
