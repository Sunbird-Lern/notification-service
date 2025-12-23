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
import org.sunbird.utils.PropertiesCache;
import scala.collection.JavaConverters;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
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
        // Convert Scala collection to Java List to avoid ClassCastException
        Object idsObject = request.getRequest().get(JsonKey.IDS);
        List<String> feedIds;
        if (idsObject instanceof scala.collection.Seq) {
            feedIds = new ArrayList<>(JavaConverters.asJavaCollectionConverter((scala.collection.Seq<String>) idsObject).asJavaCollection());
        } else {
            feedIds = (List<String>) idsObject;
        }
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
                List<Map<String, Object>> mappedFeedIdLists = notificationService.getFeedMap(feedIds, request.getContext());
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
