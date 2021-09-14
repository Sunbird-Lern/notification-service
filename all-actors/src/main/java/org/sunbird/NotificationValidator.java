package org.sunbird;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.exception.ValidationException;
import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.IUserResponseMessage;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.request.Request;
import org.sunbird.pojo.NotificationMode;
import org.sunbird.pojo.NotificationRequest;

/** Validates send notification api request */
public class NotificationValidator {
  private static final int MAX_NOTIFICATION_SIZE = 1000;

  public static void validate(NotificationRequest notificationRequest) throws BaseException {
    validateModeType(notificationRequest.getMode());
    // in case of topic based notification id not required.
    if (StringUtils.isBlank(
        notificationRequest.getConfig() != null
            ? notificationRequest.getConfig().getTopic()
            : "")) {
      validateIds(notificationRequest.getIds());
    }
    // for checking mandatory params of string type
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
    if (StringUtils.isBlank(param)) {
      throw new BaseException(
          "MANDATORY_PARAMETER_MISSING",
          MessageFormat.format(
              IResponseMessage.Message.MANDATORY_PARAMETER_MISSING, JsonKey.NOTIFICATIONS + "." + key),
          ResponseCode.CLIENT_ERROR.getCode());
    }
  }

  private static void validateModeType(String mode) throws BaseException {
    checkMandatoryParamsPresent(mode, JsonKey.MODE);
    List<String> modeType = NotificationMode.get();
    if (!modeType.contains(mode)) {
      throw new BaseException(
          "INVALID_VALUE",
          MessageFormat.format(
              IResponseMessage.INVALID_VALUE,
              JsonKey.NOTIFICATIONS + "." + JsonKey.MODE,
              "IT SHOULD BE ONE OF THE FOLLOWING VAlUES " + modeType),
          ResponseCode.CLIENT_ERROR.getCode());
    }
  }

  /**
   * validates notification ids , throws if it is null or empty or string
   *
   * @param ids
   * @throws BaseException
   */
  private static void validateIds(List<String> ids) throws BaseException {
    if (CollectionUtils.isEmpty(ids)) {
      throw new BaseException(
          "MANDATORY_PARAMETER_MISSING",
          MessageFormat.format(
              IResponseMessage.Message.MANDATORY_PARAMETER_MISSING,
              JsonKey.NOTIFICATIONS + "." + JsonKey.IDS),
          ResponseCode.CLIENT_ERROR.getCode());
    }
  }

  public static void validateRequestObject(Request request) throws BaseException {
    if (request.getRequest().isEmpty()) {
      throw new ValidationException.InvalidRequestData();
    }
  }

  public static void validateMandatoryParamsWithType(
          Map<String, Object> reqMap,
          List<String> mandatoryParamsList,
          Class<?> type,
          boolean validatePresence,
          String parentKey,
          Map<String,Object> reqContext)
          throws BaseException {
    for (String param : mandatoryParamsList) {
      if (!reqMap.containsKey(param)) {
        throw new ValidationException.MandatoryParamMissing(param, parentKey);
      }

      if (!(isInstanceOf(reqMap.get(param).getClass(), type))) {
        throw new ValidationException.ParamDataTypeError(parentKey + "." + param, type.getName());
      }

      if (validatePresence) {
        validatePresence(param, reqMap.get(param), type, parentKey,reqContext);
      }
    }
  }

  private static void validatePresence(String key, Object value, Class<?> type, String parentKey,Map<String,Object> reqContext)
          throws BaseException {
    if (type == String.class) {
      if (StringUtils.isBlank((String) value)) {
        throw new ValidationException.MandatoryParamMissing(key, parentKey);
      }
    } else if (type == Map.class) {
      Map<String, Object> map = (Map<String, Object>) value;
      if (map.isEmpty()) {
        throw new ValidationException.MandatoryParamMissing(key, parentKey);
      }
    } else if (type == List.class) {
      List<?> list = (List<?>) value;
      if (list.isEmpty()) {
        throw new ValidationException.MandatoryParamMissing(key, parentKey);
      }
    }
  }

  public static boolean isInstanceOf(Class objClass, Class targetClass) {
    return targetClass.isAssignableFrom(objClass);
  }

  public static void validateParamsWithType(Map<String, Object> reqMap, List<String> paramList, Class<?> type,
                                            String parentKey,
                                            Map<String,Object> reqContext) throws BaseException {
    for (String param : paramList) {
      if(reqMap.containsKey(param)) {
        if (!(isInstanceOf(reqMap.get(param).getClass(), type))) {
          throw new ValidationException.ParamDataTypeError(parentKey + "." + param, type.getName());
        }
      }
    }
  }


  public static void validateMaxSupportedIds(List<String> ids) throws BaseException {
    if (ids.size() > MAX_NOTIFICATION_SIZE) {
      throw new BaseException(
          IUserResponseMessage.INVALID_REQUESTED_DATA,
          MessageFormat.format(IResponseMessage.MAX_NOTIFICATION_SIZE, MAX_NOTIFICATION_SIZE),
          ResponseCode.CLIENT_ERROR.getCode());
    }
  }


  public static void validateDeleteRequest(Request request) throws BaseException{
    validateRequestObject(request);
    validateMandatoryParamsWithType(request.getRequest(), Lists.newArrayList(JsonKey.USER_ID,JsonKey.CATEGORY),String.class,true,JsonKey.REQUEST,request.getContext());
    validateParamsWithType(request.getRequest(),Lists.newArrayList(JsonKey.IDS),
            List.class, JsonKey.REQUEST,request.getContext());
  }
}
