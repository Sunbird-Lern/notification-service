package org.sunbird;

import org.apache.commons.lang3.ArrayUtils;
import org.sunbird.pojo.NotificationMode;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.message.IResponseMessage;
import org.sunbird.message.ResponseCode;
import org.sunbird.pojo.NotificationRequest;
import org.sunbird.request.Request;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;


/**
 * Validates send notification api request
 */
public class NotificationValidator {


    public static void validateSendNotificationRequest(Request request) throws BaseException {
        List<NotificationRequest> req;
        try {
            req = NotificationRequestMapper.toList((List<Map<String, Object>>) request.getRequest().get(JsonKey.NOTIFICATIONS));
        } catch (Exception e) {
            throw new BaseException("INVALID_REQUESTED_DATA",
                    MessageFormat.format(IResponseMessage.INVALID_REQUESTED_DATA, ", provide a valid request data"),
                    ResponseCode.CLIENT_ERROR.getCode());
        }
        if (req.isEmpty()) {
            throw new BaseException("MANDATORY_PARAMETER_MISSING",
                    MessageFormat.format(IResponseMessage.MANDATORY_PARAMETER_MISSING, JsonKey.NOTIFICATIONS),
                    ResponseCode.CLIENT_ERROR.getCode());
        }
        for (NotificationRequest notificationRequest : req) {
            validateSendNotificationModeType(notificationRequest.getMode());
            validateSendNotificationIds(notificationRequest.getIds());
            checkMandatoryParamsPresent(notificationRequest.getDeliveryType(), JsonKey.DELIVERY_TYPE);
        }

    }


    private static void checkMandatoryParamsPresent(String param, String key) throws BaseException {
        if (StringUtils.isEmpty(param)) {
            throw new BaseException("MANDATORY_PARAMETER_MISSING",
                    MessageFormat.format(IResponseMessage.MANDATORY_PARAMETER_MISSING,
                            JsonKey.NOTIFICATIONS + "." + key),
                    ResponseCode.CLIENT_ERROR.getCode());
        }

    }

    private static void validateSendNotificationModeType(String mode) throws BaseException {
        checkMandatoryParamsPresent(mode, JsonKey.MODE);
        List<String> modeType = NotificationMode.get();
        if (!modeType.contains(mode)) {
            throw new BaseException("INVALID_VALUE",
                    MessageFormat.format(IResponseMessage.INVALID_VALUE, JsonKey.NOTIFICATIONS + "." +
                            JsonKey.MODE, "IT SHOULD BE ONE OF THE FOLLOWING VAlUES " + modeType),
                    ResponseCode.CLIENT_ERROR.getCode());
        }
    }

    /**
     * validates notification ids , throws if it is null or empty or string
     *
     * @param ids
     * @throws BaseException
     */
    private static void validateSendNotificationIds(String[] ids) throws BaseException {
        if (ArrayUtils.isEmpty(ids)) {
            throw new BaseException("MANDATORY_PARAMETER_MISSING",
                    MessageFormat.format(IResponseMessage.MANDATORY_PARAMETER_MISSING, JsonKey.NOTIFICATIONS + "." + JsonKey.IDS),
                    ResponseCode.CLIENT_ERROR.getCode());
        }
    }

}

