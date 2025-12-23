package org.sunbird.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.collections.MapUtils;
import org.sunbird.JsonKey;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.response.Response;
import org.sunbird.notification.dispatcher.impl.FCMNotificationDispatcher;
import org.sunbird.notification.utils.NotificationConstant;
import org.sunbird.pojo.NotificationRequest;
import org.sunbird.pojo.NotificationV2Request;
import org.sunbird.service.NotificationService;

import java.text.MessageFormat;
import java.util.Map;

public class Util {

    public static Map<String, Object> getTemplate(NotificationV2Request notificationRequest, NotificationService notificationService, Map<String, Object> reqContext) throws BaseException {
        Map<String,Object> template = (Map<String, Object>) notificationRequest.getAction().get(JsonKey.TEMPLATE);
        Map<String,Object> paramObj = (Map<String, Object>) template.get(JsonKey.PARAMS);
        if(MapUtils.isEmpty(paramObj)){
            throw new BaseException(IResponseMessage.Key.MANDATORY_PARAMETER_MISSING,
                    MessageFormat.format(IResponseMessage.Message.MANDATORY_PARAMETER_MISSING,JsonKey.PARAMS), ResponseCode.CLIENT_ERROR.getCode());
        }
        if(null != template.get(JsonKey.DATA) && null == template.get(JsonKey.TYPE)){
            throw new BaseException(IResponseMessage.Key.MANDATORY_PARAMETER_MISSING,
                    MessageFormat.format(IResponseMessage.Message.MANDATORY_PARAMETER_MISSING, JsonKey.TYPE), ResponseCode.CLIENT_ERROR.getCode());
        }else if(null == template.get(JsonKey.DATA)){
            template = notificationService.getTemplate((String) notificationRequest.getAction().get(JsonKey.TYPE), reqContext);
        }
        if(null != template.get(JsonKey.TEMPLATE_SCHEMA)){
            notificationService.validateTemplate(paramObj, (String) template.get(JsonKey.TEMPLATE_SCHEMA));
        }
        if(MapUtils.isEmpty(template)){
            throw new BaseException(IResponseMessage.Key.TEMPLATE_NOT_FOUND,
                    MessageFormat.format(IResponseMessage.Message.TEMPLATE_NOT_FOUND, notificationRequest.getAction().get(JsonKey.TYPE)), ResponseCode.CLIENT_ERROR.getCode());
        }
        template.put(JsonKey.PARAMS,paramObj);
        return template;
    }

    private static com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
    private static com.fasterxml.jackson.databind.ObjectMapper javaMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    static {
        mapper.findAndRegisterModules();
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        javaMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    public static <T> java.util.List<T> convertToList(Object object, com.fasterxml.jackson.core.type.TypeReference<java.util.List<T>> typeReference) {
        try {
            JsonNode node = mapper.valueToTree(object);
            return javaMapper.convertValue(node, typeReference);
        } catch (Exception e) {
            throw new org.sunbird.common.exception.BaseException(IResponseMessage.Key.INVALID_REQUESTED_DATA,
                "Invalid data format.", ResponseCode.CLIENT_ERROR.getCode());
        }
    }

    public static Response writeDataToKafka(
            NotificationRequest notification,
            Response response,
            boolean isDryRun,
            Map<String, Object> responseMap,
            boolean isSync,
            Map<String, Object> context) {
        FCMNotificationDispatcher.getInstance().dispatch(notification, isDryRun, isSync, context);
        responseMap.put(Constant.RESPONSE, NotificationConstant.SUCCESS);
        response.putAll(responseMap);
        return response;
    }
}
