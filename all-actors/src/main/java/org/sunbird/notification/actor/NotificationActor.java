package org.sunbird.notification.actor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.BaseActor;
import org.sunbird.JsonKey;
import org.sunbird.NotificationRequestMapper;
import org.sunbird.NotificationValidator;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.common.exception.ActorServiceException;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.IUserResponseMessage;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.request.Request;
import org.sunbird.notification.beans.OTPRequest;
import org.sunbird.notification.dispatcher.NotificationRouter;
import org.sunbird.notification.utils.NotificationConstant;
import org.sunbird.pojo.NotificationRequest;
import org.sunbird.request.LoggerUtil;
import org.sunbird.common.response.Response;
import org.sunbird.util.validator.OtpRequestValidator;

/** @author manzarul */
@ActorConfig(
  tasks = {JsonKey.NOTIFICATION, JsonKey.VERIFY_OTP},
  asyncTasks = {},
  dispatcher= "notification-dispatcher"
)
public class NotificationActor extends BaseActor {
  private static LoggerUtil logger = new LoggerUtil(NotificationActor.class);

  @Override
  public void onReceive(Request request) throws Throwable {
    String operation = request.getOperation();
    if (JsonKey.NOTIFICATION.equalsIgnoreCase(operation)) {
      notify(request);
    } else if (JsonKey.VERIFY_OTP.equalsIgnoreCase(operation)) {
      verifyOtp(request);

    } else {
      onReceiveUnsupportedMessage(request.getOperation());
    }
    logger.info(request.getContext(),"onReceive method call End");
  }

  public void notify(Request request) throws BaseException {
    boolean isSyncDelivery = false;
    logger.info(request.getRequest(),"Call started for notify method");
    List<NotificationRequest> notificationRequestList =
        NotificationRequestMapper.toList(
            (List<Map<String, Object>>) request.getRequest().get(JsonKey.NOTIFICATIONS));
    List<String> ids = new ArrayList<String>();
    for (NotificationRequest notificationRequest : notificationRequestList) {
      if (CollectionUtils.isNotEmpty(notificationRequest.getIds())) {
        ids.addAll(notificationRequest.getIds());
      }
      NotificationValidator.validate(notificationRequest);
    }
    NotificationValidator.validateMaxSupportedIds(ids);
    NotificationRouter routes = new NotificationRouter();
    String deliveryMode = request.getManagerName();
    if (StringUtils.isNotBlank(deliveryMode) && "sync".equalsIgnoreCase(deliveryMode)) {
      isSyncDelivery = true;
    }
    Response response = routes.route(notificationRequestList, false, isSyncDelivery, request.getContext());
    logger.info(request.getRequest(),"response got from notification service " + response);
    sender().tell(response, getSelf());
  }

  public void verifyOtp(Request request) throws BaseException {
    logger.info(request.getRequest(),"call started for verify otp method");
    Map<String, Object> requestMap = request.getRequest();
    boolean response =
        OtpRequestValidator.isOtpVerifyRequestValid(
            (String) requestMap.get(NotificationConstant.KEY),
            (String) request.get(NotificationConstant.VALUE));
    if (!response) {
      throw new ActorServiceException.InvalidRequestData(
          IUserResponseMessage.INVALID_REQUESTED_DATA,
          MessageFormat.format(
              IResponseMessage.Message.INVALID_REQUESTED_DATA, NotificationConstant.VERIFY_OTP),
          ResponseCode.CLIENT_ERROR.getCode());
    }
    NotificationRouter routes = new NotificationRouter();
    OTPRequest otpRequest =
        new OTPRequest(
            (String) requestMap.get(NotificationConstant.KEY),
            null,
            0,
            0,
            null,
            (String) request.get(NotificationConstant.VALUE));
    Response responseData = routes.verifyOtp(otpRequest, request.getContext());
    logger.info(request.getRequest(),"response got from notification service " + response);
    sender().tell(responseData, getSelf());
  }
}
