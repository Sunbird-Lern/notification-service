package org.sunbird.notification.actor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sunbird.BaseActor;
import org.sunbird.BaseException;
import org.sunbird.JsonKey;
import org.sunbird.NotificationRequestMapper;
import org.sunbird.NotificationValidator;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.notification.dispatcher.INotificationDispatcher;
import org.sunbird.notification.dispatcher.impl.FCMNotificationDispatcher;
import org.sunbird.notification.utils.FCMResponse;
import org.sunbird.pojo.NotificationRequest;
import org.sunbird.request.Request;
import org.sunbird.response.Response;
import org.sunbird.util.Constant;

/** @author manzarul */
@ActorConfig(
  tasks = {JsonKey.NOTIFICATION},
  asyncTasks = {}
)
public class NotificationActor extends BaseActor {
  Logger logger = LogManager.getLogger(NotificationActor.class);
  private static final String NOTIFICATION = JsonKey.NOTIFICATION;
  private static final String SUNBIRD_NOTIFICATION_DEFAULT_DISPATCH_MODE =
      "sunbird_notification_default_dispatch_mode";
  private static final String SUNBIRD_NOTIFICATION_DEFAULT_DISPATCH_MODE_VAL = "async";
  INotificationDispatcher Dispatcher = new FCMNotificationDispatcher();

  @Override
  public void onReceive(Request request) throws Throwable {
    String operation = request.getOperation();
    if (NOTIFICATION.equalsIgnoreCase(operation)) {
      notify(request);
    } else {
      onReceiveUnsupportedMessage(request.getOperation());
    }

    logger.info("onReceive method call End");
  }

  public void notify(Request request) throws BaseException {
    List<NotificationRequest> notificationRequestList =
        NotificationRequestMapper.toList(
            (List<Map<String, Object>>) request.getRequest().get(JsonKey.NOTIFICATIONS));
    for (NotificationRequest notificationRequest : notificationRequestList) {
      NotificationValidator.validate(notificationRequest);
    }
    Map<String, Object> requestMap = request.getRequest();
    List<FCMResponse> responses = new ArrayList<FCMResponse>();
    Response response = new Response();
    if (System.getenv(SUNBIRD_NOTIFICATION_DEFAULT_DISPATCH_MODE) != null
        && !SUNBIRD_NOTIFICATION_DEFAULT_DISPATCH_MODE_VAL.equalsIgnoreCase(
            System.getenv(SUNBIRD_NOTIFICATION_DEFAULT_DISPATCH_MODE))) {
      responses = Dispatcher.dispatch(requestMap, false);
      response.getResult().put(Constant.RESPONSE, responses);
    } else {
      boolean resp = Dispatcher.dispatchAsync(requestMap);
      response.getResult().put(Constant.RESPONSE, resp);
    }

    sender().tell(response, getSelf());
  }
}
