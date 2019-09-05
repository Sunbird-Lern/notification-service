package controllers.notification;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.BaseException;
import org.sunbird.JsonKey;
import org.sunbird.message.IResponseMessage;
import org.sunbird.message.ResponseCode;
import org.sunbird.request.Request;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

/**
 * Validates send notification api request
 */
public class NotificationValidator {


    public static void validateSendNotificationRequest(Request request) throws BaseException {
        List<Map<String, Object>> sendReq = (List<Map<String, Object>>) request.getRequest().get(JsonKey.NOTIFICATIONS);
        checkMandatoryParamsPresent(sendReq, JsonKey.NOTIFICATIONS, Arrays.asList(JsonKey.DELIVERY_TYPE, JsonKey.MODE, JsonKey.IDS));
    }

    private static void checkMandatoryParamsPresent(
            List<Map<String, Object>> data, String parentKey, List<String> keys) throws BaseException {
        if (CollectionUtils.isEmpty(data)) {
            throw new BaseException("MANDATORY_PARAMETER_MISSING",
                    MessageFormat.format(IResponseMessage.MANDATORY_PARAMETER_MISSING, parentKey),
                    ResponseCode.CLIENT_ERROR.getCode());
        }
        for (Map<String, Object> map : data) {
            checkChildrenMapMandatoryParams(map, keys, parentKey);
        }

    }

    private static void checkChildrenMapMandatoryParams(Map<String, Object> data, List<String> keys, String parentKey) throws BaseException {

        for (String key : keys) {
            if (key.equals(JsonKey.IDS)) {
                validateSendNotificationIds(data.get(key));
            } else if (StringUtils.isEmpty((String) data.get(key))) {
                throw new BaseException("MANDATORY_PARAMETER_MISSING",
                        MessageFormat.format(IResponseMessage.MANDATORY_PARAMETER_MISSING, parentKey + "." + key),
                        ResponseCode.CLIENT_ERROR.getCode());
            }
            if (key.equals(JsonKey.MODE)) {
                checkModeValueIsValid((String) data.get(key));
            }
        }
    }

    private static void checkModeValueIsValid(String mode) throws BaseException {
        String[] modeType = {JsonKey.PHONE, JsonKey.EMAIL, JsonKey.DEVICE};
        if (!Arrays.asList(modeType).contains(mode)) {
            throw new BaseException("INVALID_VALUE",
                    MessageFormat.format(IResponseMessage.INVALID_VALUE, JsonKey.NOTIFICATIONS + "." + JsonKey.MODE, "IT SHOULD BE ONE OF THIS VAlUES " + Arrays.asList(modeType)),
                    ResponseCode.CLIENT_ERROR.getCode());
        }
    }

    /**
     * validates notification ids , throws if it is null or empty or string
     * @param ids
     * @throws BaseException
     */
    private static void validateSendNotificationIds(Object ids) throws BaseException {
        if (ids == null) {
            throw new BaseException("MANDATORY_PARAMETER_MISSING",
                    MessageFormat.format(IResponseMessage.MANDATORY_PARAMETER_MISSING, JsonKey.NOTIFICATIONS + "." + JsonKey.IDS),
                    ResponseCode.CLIENT_ERROR.getCode());
        }
        if (ids instanceof List) {
            if (((List) ids).isEmpty()) {
                throw new BaseException("MANDATORY_PARAMETER_MISSING",
                        MessageFormat.format(IResponseMessage.MANDATORY_PARAMETER_MISSING, JsonKey.NOTIFICATIONS + "." + JsonKey.IDS),
                        ResponseCode.CLIENT_ERROR.getCode());
            }
        }
        if (ids instanceof String) {
            throw new BaseException("DATA_TYPE_REQUIRED",
                    MessageFormat.format(IResponseMessage.DATA_TYPE_REQUIRED, JsonKey.NOTIFICATIONS + "." + JsonKey.IDS, "LIST"),
                    ResponseCode.CLIENT_ERROR.getCode());
        }
    }

}

