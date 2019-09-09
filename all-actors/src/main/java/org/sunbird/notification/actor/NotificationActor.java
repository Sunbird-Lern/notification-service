/**
 *
 */
package org.sunbird.notification.actor;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sunbird.*;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.message.IResponseMessage;
import org.sunbird.message.ResponseCode;
import org.sunbird.pojo.NotificationRequest;
import org.sunbird.request.Request;
import org.sunbird.response.Response;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * @author manzarul
 *
 */

@ActorConfig(
        tasks = {"notification"},
        asyncTasks = {}
)
public class NotificationActor extends BaseActor {
    Logger logger = LogManager.getLogger(NotificationActor.class);
    private static final String NOTIFICATION = "notification";

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

    public void notify(Request request) throws BaseException {
        List<NotificationRequest> notificationRequestList;
        try {
            notificationRequestList = NotificationRequestMapper.toList((List<Map<String, Object>>) request.getRequest().get(JsonKey.NOTIFICATIONS));
        } catch (Exception e) {
            throw new BaseException("INVALID_REQUESTED_DATA",
                    MessageFormat.format(IResponseMessage.INVALID_REQUESTED_DATA, ", provide a valid request data"),
                    ResponseCode.CLIENT_ERROR.getCode());
        }
        for (NotificationRequest notificationRequest : notificationRequestList) {
            NotificationValidator.validate(notificationRequest);
        }
        System.out.println("Success");
        Response response = new Response();
        response.getResult().put("response", "SUCCESS");
        sender().tell(response, getSelf());
    }

}
