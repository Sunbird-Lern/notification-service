


package org.sunbird.notification.actor;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.BaseActor;
import org.sunbird.JsonKey;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.common.exception.AuthorizationException;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.request.Request;
import org.sunbird.common.response.Response;
import org.sunbird.service.NotificationService;
import org.sunbird.service.NotificationServiceImpl;
import org.sunbird.util.RequestHandler;

import java.text.MessageFormat;
import java.util.*;

@ActorConfig(
        tasks = {JsonKey.READ_FEED},
        asyncTasks = {},
        dispatcher= "notification-dispatcher"
)
public class ReadNotificationFeedActor extends BaseActor {
    @Override
    public void onReceive(Request request) throws Throwable {
        RequestHandler requestHandler = new RequestHandler();
        String requestedBy = requestHandler.getRequestedBy(request);
        String userId = (String) request.getRequest().get(JsonKey.USER_ID);
        if(StringUtils.isEmpty(userId)){
            throw new BaseException(IResponseMessage.Key.MANDATORY_PARAMETER_MISSING,
                    MessageFormat.format(IResponseMessage.Message.MANDATORY_PARAMETER_MISSING,JsonKey.USER_ID), ResponseCode.CLIENT_ERROR.getCode());
        }
        if(!userId.equals(requestedBy)){
            //throw Authorization Exception
            throw new AuthorizationException.NotAuthorized(ResponseCode.unAuthorized);
        }
        NotificationService notificationService = NotificationServiceImpl.getInstance();
        List<Map<String, Object>> notifications = notificationService.readNotificationFeed(userId,request.getContext());
        Response response = new Response();
        response.put("feeds",notifications);
        sender().tell(response, getSelf());
    }


}





