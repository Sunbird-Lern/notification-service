


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
public class ReadNotificationActor extends BaseActor {
    private static LoggerUtil logger = new LoggerUtil(ReadNotificationActor.class);

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
                onReceiveUnsupportedMessage("ReadNotificationActor");
        }
    }

    private void readV1Feed(Request request){
        logger.info(request.getContext(),"ReadNotificationActor: readV1Feed Started");
        String requestedBy = (String) request.getRequest().get(JsonKey.USER_ID);
        readFeed(request,requestedBy);
        logger.info(request.getContext(),"ReadNotificationActor: readV1Feed Ended");

    }

    private void readV2Feed(Request request){
        logger.info(request.getContext(),"ReadNotificationActor: readV2Feed Started");
        RequestHandler requestHandler = new RequestHandler();
        String requestedBy = requestHandler.getRequestedBy(request);
        readFeed(request,requestedBy);
        logger.info(request.getContext(),"ReadNotificationActor: readV1Feed Started");

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
                logger.error(request.getContext(),MessageFormat.format("Requested by :{0} for user id : {1}",requestedBy,userId));
                throw new AuthorizationException.NotAuthorized(ResponseCode.unAuthorized);
            }
            NotificationService notificationService = NotificationServiceImpl.getInstance();
            List<Map<String, Object>> notifications = new ArrayList<>();
            if("v1".equals(version)){
                notifications = notificationService.readV1NotificationFeed(userId,request.getContext());
            }else{
                notifications = notificationService.readNotificationFeed(userId, request.getContext());
            }
            Collections.sort(notifications, new Comparator<Map<String, Object>>() {
                public int compare(final Map<String, Object> o1, final Map<String, Object> o2) {

                    return ((Date)o2.get("createdOn") != null ?
                             (Date)o2.get("createdOn") : new Date(0)).compareTo((Date)o1.get("createdOn")!= null ?
                             (Date)o1.get("createdOn") : new Date(0));
                }
            });
            Response response = new Response();
            response.put("feeds",notifications);
            sender().tell(response, getSelf());
       } catch (BaseException ex){
             logger.error(request.getContext(),MessageFormat.format(":Error Msg: {0} ",ex.getMessage()),
            ex);
             throw ex;
       }
        catch (Exception ex){
             logger.error(request.getContext(),MessageFormat.format("ReadNotificationFeedActor:Error Msg: {0} ",ex.getMessage()),
         ex);
            throw new BaseException(IResponseMessage.Key.SERVER_ERROR,IResponseMessage.Message.INTERNAL_ERROR, ResponseCode.serverError.getResponseCode());

        }
    }


}





