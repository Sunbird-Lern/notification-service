package org.sunbird.notification.actor;

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

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.*;

@ActorConfig(
        tasks = {JsonKey.UPDATE_FEED, JsonKey.UPDATE_V1_FEED},
        asyncTasks = {},
        dispatcher= "notification-dispatcher"
)
public class UpdateNotificationActor extends BaseActor {

    private static LoggerUtil logger = new LoggerUtil(UpdateNotificationActor.class);

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
                onReceiveUnsupportedMessage("UpdateNotificationActor");
        }
    }
    private void updateV1Feed(Request request){
        logger.info(request.getContext(),"UpdateNotificationActor: updateV1Feed Started");

        String requestedBy = (String) request.getRequest().get(JsonKey.USER_ID);
        updateFeed(request,requestedBy);
        logger.info(request.getContext(),"UpdateNotificationActor: updateV1Feed Ended");

    }

    private void updateV2Feed(Request request){
        logger.info(request.getContext(),"UpdateNotificationActor: updateV2Feed Started");
        RequestHandler requestHandler = new RequestHandler();
        String requestedBy = requestHandler.getRequestedBy(request);
        updateFeed(request,requestedBy);
        logger.info(request.getContext(),"UpdateNotificationActor: updateV2Feed Ended");

    }

    private void updateFeed(Request request, String requestedBy){
        String userId = (String) request.getRequest().get(JsonKey.USER_ID);
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
            boolean isSupportEnabled = Boolean.parseBoolean(PropertiesCache.getInstance().getProperty(JsonKey.VERSION_SUPPORT_CONFIG_ENABLE));
            if(isSupportEnabled) {
                List<Map<String, Object>> mappedFeedIdLists = notificationService.getFeedMap((List<String>) request.getRequest().get(JsonKey.IDS), request.getContext());
                getOtherVersionUpdatedFeedList(mappedFeedIdLists, feedsUpdateList, requestedBy);
            }
            Response response = notificationService.updateNotificationFeed(feedsUpdateList, request.getContext());
            sender().tell(response, getSelf());

        }   catch (BaseException ex){
            logger.error(request.getContext(),MessageFormat.format(":Error Msg: {0} ",ex.getMessage()),
                    ex);
            throw ex;
        }
            catch (Exception ex){
            logger.error(request.getContext(),MessageFormat.format("UpdateNotificationActor:Error Msg: {0} ",ex.getMessage()),
                    ex);
             throw new BaseException(IResponseMessage.Key.SERVER_ERROR,IResponseMessage.Message.INTERNAL_ERROR, ResponseCode.serverError.getResponseCode());
        }

    }

    /**
     * Update the other version feed to support backward compatibility
     * @param mappedFeedIdLists
     * @param feedsUpdateList
     */
    public static void getOtherVersionUpdatedFeedList(List<Map<String, Object>> mappedFeedIdLists, List<Map<String, Object>> feedsUpdateList, String requestedBy) {
        for (Map<String, Object> itr: mappedFeedIdLists){
            Map<String,Object> notificationFeed = feedsUpdateList.stream().filter(x->x.get(JsonKey.ID).equals(itr.get(JsonKey.ID))).findAny().orElse(null);
            if(MapUtils.isNotEmpty(notificationFeed)){
                Map<String,Object> feedTobeUpdated = new HashMap<>();
                feedTobeUpdated.put(JsonKey.ID,itr.get(JsonKey.FEED_ID));
                feedTobeUpdated.put(JsonKey.USER_ID,notificationFeed.get(JsonKey.USER_ID));
                feedTobeUpdated.put(JsonKey.STATUS,notificationFeed.get(JsonKey.STATUS));
                feedTobeUpdated.put(JsonKey.UPDATED_BY,requestedBy);
                feedTobeUpdated.put(JsonKey.UPDATED_ON,new Timestamp(Calendar.getInstance().getTime().getTime()));
                feedsUpdateList.add(feedTobeUpdated);
            }
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
            notificationFeed.put(JsonKey.STATUS,"read");
            updateReqList.add(notificationFeed);
        }
        return updateReqList;
    }


}

