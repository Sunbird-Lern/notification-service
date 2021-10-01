package org.sunbird;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.pojo.NotificationRequest;
import org.sunbird.request.LoggerUtil;

public class NotificationRequestMapper {

  private static ObjectMapper mapper = new ObjectMapper();

  private static LoggerUtil logger = new LoggerUtil(NotificationRequestMapper.class);

  /**
   * maps request to notification request
   *
   * @param request
   * @return
   * @throws BaseException
   */
  public static List<NotificationRequest> toList(List<Map<String, Object>> request)
      throws BaseException {
    if (request.isEmpty()) {
      throw new BaseException(
          "MANDATORY_PARAMETER_MISSING",
          MessageFormat.format(IResponseMessage.Message.MANDATORY_PARAMETER_MISSING, JsonKey.NOTIFICATIONS),
          ResponseCode.CLIENT_ERROR.getCode());
    }
    List<NotificationRequest> notificationList = new ArrayList<>();
    for (Map<String, Object> map : request) {
      notificationList.add((getNotificationRequest(map)));
    }
    return notificationList;
  }

  private static NotificationRequest getNotificationRequest(Map<String, Object> data)
      throws BaseException {
    try {
      NotificationRequest notificationRequest =
          mapper.convertValue(data, NotificationRequest.class);
      return notificationRequest;
    } catch (Exception e) {
      throw new BaseException(
          "INVALID_REQUESTED_DATA",
          MessageFormat.format(
              IResponseMessage.Message.INVALID_REQUESTED_DATA,
              ", provide a valid request data, " + e.getMessage()),
          ResponseCode.CLIENT_ERROR.getCode());
    }
  }
}
