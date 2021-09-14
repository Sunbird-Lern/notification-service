package org.sunbird.notification.actor;

import org.apache.commons.collections.CollectionUtils;
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

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.*;

@ActorConfig(
        tasks = {JsonKey.UPDATE_FEED, JsonKey.UPDATE_V1_FEED},
        asyncTasks = {},
        dispatcher= "notification-dispatcher"
)
public class UpdateNotificationActor extends BaseActor {

    private static LoggerUtil logger = new LoggerUtil(ReadNotificationActor.class);

    @Override
    public void onReceive(Request request) throws Throwable {
        String operation = request.getOperation();
        switch (operation) {
            case "updateFeed":
                updateV2Feed(request);
                break;
            case "updateV1Feed":
                updateV1Feed(request);
            default:
                onReceiveUnsupportedMessage("ReadGroupActor");
        }
    }
    private void updateV1Feed(Request request){
        String requestedBy = (String) request.getRequest().get(JsonKey.USER_ID);
        updateFeed(request,requestedBy);
    }

    private void updateV2Feed(Request request){
        RequestHandler requestHandler = new RequestHandler();
        String requestedBy = requestHandler.getRequestedBy(request);
        updateFeed(request,requestedBy);
    }

    private void updateFeed(Request request, String requestedBy){
        String userId = (String) request.getRequest().get(JsonKey.USER_ID);
        request.getRequest().put(JsonKey.STATUS,"read");
        try {
            if (StringUtils.isEmpty(userId)) {
                throw new BaseException(IResponseMessage.Key.MANDATORY_PARAMETER_MISSING,
                        MessageFormat.format(IResponseMessage.Message.MANDATORY_PARAMETER_MISSING, JsonKey.USER_ID), ResponseCode.CLIENT_ERROR.getCode());
            }
            if (!userId.equals(requestedBy)) {
                //throw Authorization Exception
                throw new AuthorizationException.NotAuthorized(ResponseCode.unAuthorized);
            }

            List<Map<String, Object>> feedsUpdateList = createUpdateStatusReq(request.getRequest(), requestedBy);
            if (!CollectionUtils.isNotEmpty(feedsUpdateList)) {
                throw new BaseException(IResponseMessage.Key.MANDATORY_PARAMETER_MISSING,
                        MessageFormat.format(IResponseMessage.Message.MANDATORY_PARAMETER_MISSING, JsonKey.IDS), ResponseCode.CLIENT_ERROR.getCode());
            }
            NotificationService notificationService = NotificationServiceImpl.getInstance();
            Response response = notificationService.updateNotificationFeed(feedsUpdateList, request.getContext());
            sender().tell(response, getSelf());

        }   catch (BaseException ex){
            logger.error(MessageFormat.format(":Error Msg: {0} ",ex.getMessage()),
                    ex);
            throw ex;
        }
            catch (Exception ex){
            logger.error(MessageFormat.format("UpdateNotificationActor:Error Msg: {0} ",ex.getMessage()),
                    ex);
             throw new BaseException(IResponseMessage.Key.SERVER_ERROR,IResponseMessage.Message.INTERNAL_ERROR, ResponseCode.serverError.getResponseCode());
        }

    }

    private  List<Map<String,Object>>  createUpdateStatusReq(Map<String, Object> request, String requestedBy) {

        List<Map<String,Object>> updateReqList = new ArrayList<>();
        List<String> feedIds = (List<String>) request.get(JsonKey.IDS);
        for (String feedId: feedIds) {
            Map<String,Object> notificationFeed= new HashMap<>();
            notificationFeed.put(JsonKey.ID,feedId);
            notificationFeed.put(JsonKey.USER_ID,(String) request.get(JsonKey.USER_ID));
            notificationFeed.put(JsonKey.UPDATED_BY,requestedBy);
            notificationFeed.put(JsonKey.UPDATED_ON,new Timestamp(Calendar.getInstance().getTime().getTime()));
            notificationFeed.put(JsonKey.STATUS,(String) request.get(JsonKey.STATUS));
            updateReqList.add(notificationFeed);
        }
        return updateReqList;
    }


}

