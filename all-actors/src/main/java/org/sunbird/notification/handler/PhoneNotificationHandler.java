package org.sunbird.notification.handler;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.sunbird.JsonKey;
import org.sunbird.common.exception.ActorServiceException;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.IUserResponseMessage;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.notification.beans.OTPRequest;
import org.sunbird.notification.dispatcher.NotificationRouter;
import org.sunbird.notification.dispatcher.SyncMessageDispatcher;
import org.sunbird.notification.utils.NotificationConstant;
import org.sunbird.pojo.*;
import org.sunbird.request.LoggerUtil;
import org.sunbird.common.response.Response;
import org.sunbird.service.NotificationService;
import org.sunbird.service.NotificationServiceImpl;
import org.sunbird.util.Util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhoneNotificationHandler implements INotificationHandler{
    private static LoggerUtil logger = new LoggerUtil(PhoneNotificationHandler.class);
    private NotificationService notificationService = NotificationServiceImpl.getInstance();
    private SyncMessageDispatcher syDispatcher = new SyncMessageDispatcher();
    ObjectMapper mapper = new ObjectMapper();

    @Override
    public Response sendNotification(NotificationV2Request notificationRequest, boolean isDryRun, boolean isSync, Map<String,Object> reqContext) throws BaseException {
        logger.info("PhoneNotificationHandler: making call to sendNotifications method");
        Response response = new Response();
        if(null != notificationRequest && CollectionUtils.isNotEmpty(notificationRequest.getIds())){
            Map<String, Object> responseMap = new HashMap<String, Object>();
            Map<String, Object> template = Util.getTemplate(notificationRequest, notificationService, reqContext);
            Map<String, Object> dataTemplate = new HashMap<>();
            dataTemplate.put(JsonKey.VER, template.get(JsonKey.VER));
            dataTemplate.put(JsonKey.TYPE, template.get(JsonKey.TYPE));
            dataTemplate.put(JsonKey.DATA,
                    notificationService.transformTemplate((String) template.get(JsonKey.DATA), (Map<String, Object>) template.get(JsonKey.PARAMS)));
            dataTemplate.put(JsonKey.PARAMS,template.get(JsonKey.PARAMS));
            notificationRequest.getAction().setTemplate(dataTemplate);
            NotificationRequest notification = createNotificationObj(notificationRequest);
            if (notification.getDeliveryType().equals(NotificationRouter.DeliveryType.otp.name())) {
                responseMap = handleOTP(notification, reqContext);
                response.putAll(responseMap);
            } else {
                response = handleMessage(isDryRun, isSync, reqContext, response, responseMap, notification);
            }
        }
        return response;
    }

    private Response handleMessage(boolean isDryRun, boolean isSync, Map<String, Object> reqContext, Response response, Map<String, Object> responseMap, NotificationRequest notification) {
        if (isSync) {
            response = syDispatcher.syncDispatch(notification,  reqContext);
        } else {
            response = Util.writeDataToKafka(notification, response, isDryRun, responseMap, isSync, reqContext);
        }
        return response;
    }

    private NotificationRequest createNotificationObj(NotificationV2Request notificationRequest) {
        NotificationRequest notification = new NotificationRequest();
        notification.setIds(notificationRequest.getIds());
        notification.setMode(NotificationRouter.DeliveryMode.phone.name());
        Config config = new Config();
        Map<String,Object> otp = (Map<String, Object>) notificationRequest.getAction().getAdditionalInfo().get(JsonKey.OTP);
        if (MapUtils.isNotEmpty(otp)){
            notification.setDeliveryType(NotificationRouter.DeliveryType.otp.name());
            config.setOtp(mapper.convertValue(otp,OTP.class));
        }else{
            notification.setDeliveryType(NotificationRouter.DeliveryType.message.name());
        }
        config.setSubject(notificationRequest.getAction().getAdditionalInfo().get(JsonKey.SUBJECT) !=null ?
                (String)notificationRequest.getAction().getAdditionalInfo().get(JsonKey.SUBJECT): null);
        config.setSender(notificationRequest.getAction().getAdditionalInfo().get(JsonKey.SENDER) !=null ?
                (String)notificationRequest.getAction().getAdditionalInfo().get(JsonKey.SENDER): null);
        notification.setConfig(config);
        Template template = new Template();
        template.setData((String) notificationRequest.getAction().getTemplate().get(JsonKey.DATA));
        template.setParams(mapper.convertValue((Map<String,Object>)notificationRequest.getAction()
                .getTemplate().get(JsonKey.PARAMS), JsonNode.class));
        notification.setTemplate(template);
        return notification;
    }

    private Map<String, Object> handleOTP(NotificationRequest notification, Map<String,Object> reqContext) throws ActorServiceException.InvalidRequestData {
        Map<String, Object> responseMap = new HashMap<String, Object>();
        Config config = notification.getConfig();
        Template template = notification.getTemplate();
        OTP otp = config.getOtp();
        List<String> ids = notification.getIds();
        if (ids.size() > 1) {
            throw new ActorServiceException.InvalidRequestData(
                    IUserResponseMessage.USER_NOT_FOUND,
                    MessageFormat.format(
                            IResponseMessage.Message.INVALID_REQUESTED_DATA, NotificationConstant.OTP_PHONE_ERROR),
                    ResponseCode.CLIENT_ERROR.getCode());
        }
        OTPRequest request =
                new OTPRequest(ids.get(0), null, otp.getLength(), otp.getExpiryInMinute(), template.getData(), null);
        boolean smsResponse = new NotificationRouter().getSMSInstance().sendOtp(request, reqContext);
        responseMap.put(ids.get(0),smsResponse);
        return responseMap;
    }
}
