


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
import org.sunbird.request.LoggerUtil;
import org.sunbird.service.NotificationService;
import org.sunbird.service.NotificationServiceImpl;
import org.sunbird.util.RequestHandler;

import java.text.MessageFormat;
import java.util.*;

@ActorConfig(
        tasks = {JsonKey.READ_FEED, JsonKey.READ_V1_FEED},
        asyncTasks = {},
        dispatcher= "notification-dispatcher"
)
public class ReadNotificationFeedActor extends BaseActor {
    private static LoggerUtil logger = new LoggerUtil(ReadNotificationFeedActor.class);

    @Override
    public void onReceive(Request request) throws Throwable {
        String operation = request.getOperation();
        switch (operation) {
            case "readFeed":
                readV2Feed(request);
                break;
            case "readV1Feed":
                readV1Feed(request);
            default:
                onReceiveUnsupportedMessage("ReadGroupActor");
        }
    }

    private void readV1Feed(Request request){
        String requestedBy = (String) request.getRequest().get(JsonKey.USER_ID);
        readFeed(request,requestedBy);
    }

    private void readV2Feed(Request request){
        RequestHandler requestHandler = new RequestHandler();
        String requestedBy = requestHandler.getRequestedBy(request);
        readFeed(request,requestedBy);
    }

    private void readFeed(Request request, String requestedBy)  {

        try{
            String userId = (String) request.getRequest().get(JsonKey.USER_ID);
            String version = (String) request.getRequest().get(JsonKey.VERSION);
            if(StringUtils.isEmpty(userId)){
                throw new BaseException(IResponseMessage.Key.MANDATORY_PARAMETER_MISSING,
                        MessageFormat.format(IResponseMessage.Message.MANDATORY_PARAMETER_MISSING,JsonKey.USER_ID), ResponseCode.CLIENT_ERROR.getCode());
            }
            if(!userId.equals(requestedBy)){
                //throw Authorization Exception
                throw new AuthorizationException.NotAuthorized(ResponseCode.unAuthorized);
            }
            NotificationService notificationService = NotificationServiceImpl.getInstance();
            List<Map<String, Object>> notifications = new ArrayList<>();
            if("v1".equals(version)){
                notifications = notificationService.readV1NotificationFeed(userId,request.getContext());
            }else{
                notifications = notificationService.readNotificationFeed(userId, request.getContext());
            }

            Response response = new Response();
            response.put("userFeed",notifications);
            sender().tell(response, getSelf());
       } catch (BaseException ex){
             logger.error(MessageFormat.format(":Error Msg: {0} ",ex.getMessage()),
            ex);
             throw ex;
       }
        catch (Exception ex){
             logger.error(MessageFormat.format("ReadNotificationFeedActor:Error Msg: {0} ",ex.getMessage()),
         ex);
            throw new BaseException(IResponseMessage.Key.SERVER_ERROR,IResponseMessage.Message.INTERNAL_ERROR, ResponseCode.serverError.getResponseCode());

        }
    }


}





