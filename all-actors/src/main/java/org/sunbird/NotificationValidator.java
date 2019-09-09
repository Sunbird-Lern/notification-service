package org.sunbird;

import org.apache.commons.lang3.ArrayUtils;
import org.sunbird.pojo.NotificationMode;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.message.IResponseMessage;
import org.sunbird.message.ResponseCode;
import org.sunbird.pojo.NotificationRequest;

import java.text.MessageFormat;
import java.util.List;


/**
 * Validates send notification api request
 */
public class NotificationValidator {


    public static void validate(NotificationRequest notificationRequest) throws BaseException {
        validateModeType(notificationRequest.getMode());
        validateIds(notificationRequest.getIds());
        //for checking mandatory params of string type
        checkMandatoryParamsPresent(notificationRequest.getDeliveryType(), JsonKey.DELIVERY_TYPE);
    }


    /**
     * validates mandatory params of type string
     *
     * @param param
     * @param key
     * @throws BaseException
     */
    private static void checkMandatoryParamsPresent(String param, String key) throws BaseException {
        if (StringUtils.isEmpty(param)) {
            throw new BaseException("MANDATORY_PARAMETER_MISSING",
                    MessageFormat.format(IResponseMessage.MANDATORY_PARAMETER_MISSING,
                            JsonKey.NOTIFICATIONS + "." + key),
                    ResponseCode.CLIENT_ERROR.getCode());
        }

    }

    private static void validateModeType(String mode) throws BaseException {
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
    private static void validateIds(String[] ids) throws BaseException {
        if (ArrayUtils.isEmpty(ids)) {
            throw new BaseException("MANDATORY_PARAMETER_MISSING",
                    MessageFormat.format(IResponseMessage.MANDATORY_PARAMETER_MISSING, JsonKey.NOTIFICATIONS + "." + JsonKey.IDS),
                    ResponseCode.CLIENT_ERROR.getCode());
        }
    }

}

