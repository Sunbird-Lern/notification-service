/** */
package org.sunbird.notification.actor;

import java.util.Map;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sunbird.BaseActor;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.notification.dispatcher.INotificationDispatcher;
import org.sunbird.notification.dispatcher.impl.FCMNotificationDispatcher;
import org.sunbird.request.Request;
import org.sunbird.response.Response;

/** @author manzarul */
@ActorConfig(
  tasks = {"notification"},
  asyncTasks = {}
)
public class NotificationActor extends BaseActor {
  Logger logger = LogManager.getLogger(NotificationActor.class);
  private static final String NOTIFICATION = "notification";
  INotificationDispatcher Dispatcher = new FCMNotificationDispatcher();

  @Override
  public void onReceive(Request request) throws Throwable {
    String operation = request.getOperation();
    logger.info("onReceive method call start for operation " + operation);
    if (NOTIFICATION.equalsIgnoreCase(operation)) {
      notify(request);
    } else {
      onReceiveUnsupportedMessage(request.getOperation());
    }

    logger.info("onReceive method call End");
  }

  public void notify(Request request) {
    logger.info("notify method called started.");
    Map<String, Object> requestMap = request.getRequest();
    Dispatcher.dispatch(requestMap, false);
    Response response = new Response();
    response.getResult().put("response", "SUCCESS");
    sender().tell(response, getSelf());
  }
}
