/**
 *
 */
package org.sunbird.notification.actor;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.sunbird.BaseActor;
import org.sunbird.BaseException;
import org.sunbird.JsonKey;
import org.sunbird.NotificationRequestMapper;
import org.sunbird.NotificationValidator;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.pojo.NotificationRequest;
import org.sunbird.request.Request;
import org.sunbird.response.Response;


import java.util.List;
import java.util.Map;

/**
 * @author manzarul
 *
 */

@ActorConfig(
        tasks = {JsonKey.NOTIFICATION},
        asyncTasks = {}
)
public class NotificationActor extends BaseActor {
    Logger logger = LogManager.getLogger(NotificationActor.class);
    private static final String NOTIFICATION = JsonKey.NOTIFICATION;

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
        List<NotificationRequest> notificationRequestList = NotificationRequestMapper
                .toList((List<Map<String, Object>>) request.getRequest().get(JsonKey.NOTIFICATIONS));
        for (NotificationRequest notificationRequest : notificationRequestList) {
            NotificationValidator.validate(notificationRequest);
        }
        Response response = new Response();
        response.getResult().put("response", "SUCCESS");
        sender().tell(response, getSelf());
    }

}
