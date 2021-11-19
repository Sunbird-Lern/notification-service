package org.sunbird.notification.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.internal.NonNull;
import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.collections.MapUtils;
import org.mockito.internal.matchers.Not;
import org.sunbird.JsonKey;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.pojo.NotificationFeed;
import org.sunbird.pojo.NotificationType;
import org.sunbird.pojo.NotificationV2Request;
import org.sunbird.request.LoggerUtil;
import org.sunbird.common.response.Response;
import org.sunbird.service.NotificationService;
import org.sunbird.service.NotificationServiceImpl;
import org.sunbird.util.Util;
import org.sunbird.utils.PropertiesCache;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class FeedNotificationHandler implements INotificationHandler{
    private static LoggerUtil logger = new LoggerUtil(FeedNotificationHandler.class);
    NotificationService notificationService = NotificationServiceImpl.getInstance();
    ObjectMapper mapper = new ObjectMapper();
    @Override
    public Response sendNotification(NotificationV2Request notificationRequest, boolean isDryRun, boolean isSync, Map<String,Object> reqContext) throws BaseException, IOException {

        logger.info("FeedNotificationHandler: making call to sendNotifications method");
        Response response = new Response();
        if(null != notificationRequest && CollectionUtils.isNotEmpty(notificationRequest.getIds())){
            Map<String,Object> template = Util.getTemplate(notificationRequest, notificationService, reqContext);
            Map<String,Object> dataTemplate =new HashMap<>();
             dataTemplate.put(JsonKey.VER,template.get(JsonKey.VER));
             dataTemplate.put(JsonKey.TYPE,template.get(JsonKey.TYPE));
             dataTemplate.put(JsonKey.DATA,
                       notificationService.transformTemplate((String)template.get(JsonKey.DATA),(Map<String, Object>) template.get(JsonKey.PARAMS)));
             notificationRequest.getAction().put(JsonKey.TEMPLATE,dataTemplate);
             String notificationActionType = PropertiesCache.getInstance().getProperty(JsonKey.NOTIFICATION_CATEGORY_TYPE_CONFIG);
             List<NotificationFeed> newFeedList = generateV2FeedListObj(notificationRequest);
             response = notificationService.createNotificationFeed(newFeedList,reqContext);
             boolean isSupportEnabled = Boolean.parseBoolean(PropertiesCache.getInstance().getProperty(JsonKey.VERSION_SUPPORT_CONFIG_ENABLE));
             if(isSupportEnabled && null != response && notificationActionType.contains((String)notificationRequest.getAction().get(JsonKey.TYPE))){
                //Write data into v1 format
                Map<String,Object> notification = transformV2toV1Notification(notificationRequest);
                List<NotificationFeed> oldFeedList = generateV1FeedListObj(notification);
                notificationService.createV1NotificationFeed(oldFeedList,reqContext);
                response =notificationService.mapV1V2Feed(newFeedList,oldFeedList,reqContext);
            }
            Map<String,List<String>> feedsToBeDeleted = new HashMap<>();
            for (NotificationFeed feed : newFeedList) {
                List<Map<String, Object>> feeds = notificationService.readNotificationFeed(feed.getUserId(), reqContext);
                getMaxLimitExceededFeed(feedsToBeDeleted,feeds);
            }
            deleteUserFeed(feedsToBeDeleted, isSupportEnabled, reqContext);
        }
        return response;
    }


    public Response sendV1Notification(Map<String, Object> notification, Map<String, Object> reqContext) throws IOException {
        logger.info("FeedNotificationHandler: making call to sendV1Notifications method");
        Response response = new Response();
        if(MapUtils.isNotEmpty(notification)){
            notification.put(JsonKey.USER_ID,Arrays.asList((String)notification.get(JsonKey.USER_ID)));
            List<NotificationFeed> oldFeedList = generateV1FeedListObj(notification);
            response = notificationService.createV1NotificationFeed(oldFeedList,reqContext);
            if(null != response){
                NotificationV2Request notificationV2Request = transformV1toV2Notification(notification);
                List<NotificationFeed> newFeedList = generateV2FeedListObj(notificationV2Request);
                notificationService.createNotificationFeed(newFeedList,reqContext);
                response = notificationService.mapV1V2Feed(newFeedList,oldFeedList,reqContext);
            }
            Map<String,List<String>> feedsToBeDeleted = new HashMap<>();
            for (NotificationFeed feed : oldFeedList) {
                List<Map<String, Object>> feeds = notificationService.readV1NotificationFeed(feed.getUserId(), reqContext);
                getMaxLimitExceededFeed(feedsToBeDeleted,feeds);
            }
            deleteUserFeed(feedsToBeDeleted, true, reqContext);

        }

        return response;
    }

    private void deleteUserFeed(Map<String,List<String>> feedListMap, boolean isSupportEnabled, Map<String,Object> reqContext) throws IOException {
         if(MapUtils.isNotEmpty(feedListMap)){
             List<String> feedList = new ArrayList<>();
             for (List<String> feed: feedListMap.values()) {
                 feedList.addAll(feed);
             }
             if (isSupportEnabled) {
                List<Map<String, Object>> mappedFeedIdLists = notificationService.getFeedMap(feedList, reqContext);
                List<String> feedIds = mappedFeedIdLists.stream().map(x -> x.get(JsonKey.FEED_ID)).filter(Objects::nonNull).map(Object::toString)
                        .collect(Collectors.toList());
                 feedList.addAll(feedIds);
            }
            notificationService.deleteNotificationFeed(feedListMap, reqContext);
            if(isSupportEnabled) {
                 notificationService.deleteNotificationFeedMap(feedList, reqContext);
            }
        }

    }

    private void getMaxLimitExceededFeed(Map<String,List<String>> feedListMap, List<Map<String, Object>> feeds) {
        if (feeds.size() >= Integer.parseInt(PropertiesCache.getInstance().getProperty(JsonKey.FEED_LIMIT))) {
            List<String> feedList = new ArrayList<>();
             Collections.sort(feeds, new Comparator<Map<String, Object>>() {
                 public int compare(final Map<String, Object> o1, final Map<String, Object> o2) {
                     return ((Date)o1.get("createdOn")).compareTo((Date)o2.get("createdOn"));
                 }
             });
             Map<String, Object> feedMap = feeds.get(0);
             feedList.add((String) feedMap.get(JsonKey.ID));
             feedListMap.put((String)feedMap.get(JsonKey.USER_ID),feedList);
         }
    }

    private List<NotificationFeed> generateV2FeedListObj(NotificationV2Request request){
        List<String> ids = request.getIds();
        List<NotificationFeed> feedList = new ArrayList<>();
        for (String id: ids) {
            NotificationFeed feed = new NotificationFeed();
            feed.setId(UUID.randomUUID().toString());
            feed.setPriority(request.getPriority());
            feed.setStatus("unread");
            feed.setCategory((String) request.getAction().get(JsonKey.CATEGORY));
            feed.setCreatedBy((String) ((Map<String,Object>)request.getAction().get(JsonKey.CREATED_BY)).get(JsonKey.ID));
            feed.setCreatedOn(new Timestamp(Calendar.getInstance().getTime().getTime()));
            feed.setUserId(id);
            feed.setAction(getAction(request.getAction()));
            feedList.add(feed);
        }
        return feedList;
    }

    private List<NotificationFeed> generateV1FeedListObj(Map<String,Object> notificationV1Req) throws JsonProcessingException {
      List<NotificationFeed> feedList = new ArrayList<>();
      List<String> userIds = (List<String>) notificationV1Req.get(JsonKey.USER_ID);
        for (String userId: userIds) {
            NotificationFeed feed = new NotificationFeed();
            feed.setId(UUID.randomUUID().toString());
            feed.setPriority((Integer) notificationV1Req.get(JsonKey.PRIORITY));
            feed.setStatus("unread");
            feed.setCategory((String) notificationV1Req.get(JsonKey.CATEGORY));
            feed.setCreatedBy((String) notificationV1Req.get(JsonKey.CREATED_BY));
            feed.setCreatedOn(new Timestamp(Calendar.getInstance().getTime().getTime()));
            feed.setUserId(userId);
            feed.setAction(new ObjectMapper().writeValueAsString((Map<String,Object>)notificationV1Req.get(JsonKey.DATA)));
            feed.setVersion("v1");
            feedList.add(feed);
        }


      return feedList;
    }

    private String getAction(Map<String,Object> action) throws BaseException {
        try {
            return mapper.writeValueAsString(action);
        }catch (JsonProcessingException ex){
            logger.error("Error while action processing",ex);
            throw new BaseException(IResponseMessage.INTERNAL_ERROR,ex.getMessage(), ResponseCode.SERVER_ERROR.getCode());

        }
    }

    private NotificationV2Request transformV1toV2Notification(Map<String,Object> notification) throws JsonProcessingException {
        NotificationV2Request notificationV2Request = new NotificationV2Request();
        notificationV2Request.setIds((List<String>)notification.get(JsonKey.USER_ID));
        notificationV2Request.setPriority((int)notification.get(JsonKey.PRIORITY));
        notificationV2Request.setType(NotificationType.FEED.getValue());
        Map<String,Object> actionMap = new HashMap<>();
        Map<String,Object> dataMap = (Map<String, Object>) notification.get(JsonKey.DATA);
        Map<String,Object> actionDataMap = (Map<String, Object>) dataMap.get(JsonKey.ACTION_DATA);
        Map<String,Object> additionalInfo = new HashMap<>();
        Map<String,Object> templateData = new HashMap<>();
        for (Map.Entry<String,Object> itr: dataMap.entrySet()) {
            if(JsonKey.ACTION_DATA.equals(itr.getKey())) {
                for (Map.Entry<String,Object> itrKey: actionDataMap.entrySet()) {
                    if (JsonKey.TITLE.equals(itrKey.getKey()) || JsonKey.DESCRIPTION.equals(itrKey.getKey())) {
                        templateData.put(itrKey.getKey(), itrKey.getValue());
                    } else {
                        additionalInfo.put(itrKey.getKey(), itrKey.getValue());
                    }
                }
            }else{
                additionalInfo.put(itr.getKey(), itr.getValue());
            }
        }
        Map<String,Object> createdBy = new HashMap<>();
        createdBy.put(JsonKey.ID,notification.get(JsonKey.CREATED_BY));
        createdBy.put(JsonKey.TYPE,"system");
        Map<String,Object> template = new HashMap<>();
        template.put(JsonKey.DATA,new ObjectMapper().writeValueAsString(templateData));
        template.put(JsonKey.TYPE,"JSON");
        template.put(JsonKey.VER, PropertiesCache.getConfigValue("telemetry_pdata_ver"));
        actionMap.put(JsonKey.CREATED_BY,createdBy);
        actionMap.put(JsonKey.TEMPLATE,template);
        actionMap.put(JsonKey.ADDITIONAL_INFO,additionalInfo);
        actionMap.put(JsonKey.TYPE,actionDataMap.get(JsonKey.ACTION_TYPE));
        actionMap.put(JsonKey.CATEGORY,notification.get(JsonKey.CATEGORY));
        notificationV2Request.setAction(actionMap);
        return notificationV2Request;
    }

    private Map<String,Object> transformV2toV1Notification(NotificationV2Request notificationV2Request) throws IOException {
        Map<String,Object> notification = new HashMap<>();
        notification.put(JsonKey.USER_ID,notificationV2Request.getIds());
        notification.put(JsonKey.PRIORITY,notificationV2Request.getPriority());
        notification.put(JsonKey.CATEGORY,notificationV2Request.getAction().get(JsonKey.CATEGORY));
        notification.put(JsonKey.CREATED_BY, ((Map<String,Object>)notificationV2Request.getAction().get(JsonKey.CREATED_BY)).get(JsonKey.ID));
        Map<String,Object> dataMap = new HashMap<>();
        String templateStr = (String) ((Map<String,Object>)notificationV2Request.getAction().get(JsonKey.TEMPLATE)).get(JsonKey.DATA);
        Map<String,Object> actionDataMap = mapper.readValue(templateStr,Map.class);
        Map<String,Object> additionalInfo = (Map<String,Object>)notificationV2Request.getAction().get(JsonKey.ADDITIONAL_INFO);
        for (Map.Entry<String,Object> itr: additionalInfo.entrySet()) {
            actionDataMap.put(itr.getKey(),itr.getValue());
        }
        actionDataMap.put(JsonKey.ACTION_TYPE,notificationV2Request.getAction().get(JsonKey.TYPE));
        dataMap.put(JsonKey.ACTION_DATA,actionDataMap);
        notification.put(JsonKey.DATA,dataMap);
        return notification;
    }

}
