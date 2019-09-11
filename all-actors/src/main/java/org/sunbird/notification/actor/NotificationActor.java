package org.sunbird.notification.actor;

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
    logger.info("Call started for notify method");
    List<NotificationRequest> notificationRequestList =
        NotificationRequestMapper.toList(
            (List<Map<String, Object>>) request.getRequest().get(JsonKey.NOTIFICATIONS));
    for (NotificationRequest notificationRequest : notificationRequestList) {
      NotificationValidator.validate(notificationRequest);
    }
    Map<String, Object> requestMap = request.getRequest();
    List<FCMResponse> responses = null;
    Response response = new Response();
    responses = Dispatcher.dispatch(requestMap, false);
    response.getResult().put(Constant.RESPONSE, responses);
    logger.info("response got from notification service " + responses);
    sender().tell(response, getSelf());
  }
}
