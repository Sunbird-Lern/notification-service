package org.sunbird.notification.actor;

import akka.actor.AbstractActor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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
import org.sunbird.util.Util;
import org.sunbird.utils.PropertiesCache;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ActorConfig(
        tasks = {JsonKey.DELETE_FEED,JsonKey.DELETE_V1_FEED},
        asyncTasks = {},
        dispatcher= "notification-dispatcher"
)
public class DeleteNotificationActor extends BaseActor {
    private static LoggerUtil logger = new LoggerUtil(DeleteNotificationActor.class);

    @Override
    public void onReceive(Request request) throws Throwable {
        String operation = request.getOperation();
        switch (operation) {
            case "deleteFeed":
                deleteV2Feed(request);
                break;
            case "deleteV1Feed":
                deleteV1Feed(request);
            default:
                onReceiveUnsupportedMessage("DeleteNotificationActor");
        }
    }

    private void deleteV2Feed(Request request) {
        logger.info(request.getContext(),"DeleteNotificationActor: deleteV2Feed Started");

        RequestHandler requestHandler = new RequestHandler();
        String requestedBy = requestHandler.getRequestedBy(request);
        deleteFeed(request,requestedBy);
        logger.info(request.getContext(),"DeleteNotificationActor: deleteV2Feed Ended");

    }

    private void deleteV1Feed(Request request) {
        logger.info(request.getContext(),"DeleteNotificationActor: deleteV1Feed Started");
        String requestedBy = (String) request.getRequest().get(JsonKey.USER_ID);
        deleteFeed(request,requestedBy);
        logger.info(request.getContext(),"DeleteNotificationActor: deleteV1Feed Ended");

    }

    private void deleteFeed(Request request, String requestedBy){
        String userId = (String) request.getRequest().get(JsonKey.USER_ID);
        List<String> feedIds = (List<String>)request.getRequest().get(JsonKey.IDS);
        try {
            if (StringUtils.isEmpty(userId)) {
                throw new BaseException(IResponseMessage.Key.MANDATORY_PARAMETER_MISSING,
                        MessageFormat.format(IResponseMessage.Message.MANDATORY_PARAMETER_MISSING, JsonKey.USER_ID), ResponseCode.CLIENT_ERROR.getCode());
            }
            if (!userId.equals(requestedBy)) {
                //throw Authorization Exception
                throw new AuthorizationException.NotAuthorized(ResponseCode.unAuthorized);
            }

            NotificationService notificationService = NotificationServiceImpl.getInstance();
            boolean isSupportEnabled = Boolean.parseBoolean(PropertiesCache.getInstance().getProperty(JsonKey.VERSION_SUPPORT_CONFIG_ENABLE));
            if(isSupportEnabled) {
                List<Map<String, Object>> mappedFeedIdLists = notificationService.getFeedMap((List<String>) request.getRequest().get(JsonKey.IDS), request.getContext());
                getOtherVersionUpdatedFeedList(mappedFeedIdLists,feedIds);
            }
            Response response = notificationService.deleteNotificationFeed(Collections.singletonMap(requestedBy,feedIds), request.getContext());
            if(isSupportEnabled){
                notificationService.deleteNotificationFeedMap(feedIds,request.getContext());
            }
            sender().tell(response, getSelf());

        }   catch (BaseException ex){
            logger.error(request.getContext(),MessageFormat.format(":Error Msg: {0} ",ex.getMessage()),
                    ex);
            throw ex;
        }
        catch (Exception ex){
            logger.error(request.getContext(),MessageFormat.format("DeleteNotificationActor:Error Msg: {0} ",ex.getMessage()),
                    ex);
            throw new BaseException(IResponseMessage.Key.SERVER_ERROR,IResponseMessage.Message.INTERNAL_ERROR, ResponseCode.serverError.getResponseCode());
        }

    }

    private void getOtherVersionUpdatedFeedList(List<Map<String, Object>> mappedFeedIdLists, List<String> feedIds) {
        for (Map<String, Object> itr: mappedFeedIdLists){
            feedIds.add((String) itr.get(JsonKey.FEED_ID));
        }
    }
}
