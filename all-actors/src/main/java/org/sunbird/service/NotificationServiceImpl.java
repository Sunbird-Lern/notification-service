package org.sunbird.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.mockito.internal.matchers.Not;
import org.sunbird.JsonKey;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.util.Notification;
import org.sunbird.dao.NotificationDao;
import org.sunbird.dao.NotificationDaoImpl;
import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.pojo.ActionData;
import org.sunbird.pojo.NotificationFeed;
import org.sunbird.pojo.NotificationType;
import org.sunbird.pojo.NotificationV2Request;
import org.sunbird.request.LoggerUtil;
import org.sunbird.common.response.Response;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.*;

public class NotificationServiceImpl implements NotificationService {
    private static LoggerUtil logger = new LoggerUtil(NotificationServiceImpl.class);
    private static NotificationService notificationService = null;
    private ObjectMapper mapper = new ObjectMapper();

    private static NotificationDao notificationDao = NotificationDaoImpl.getInstance();
    public static NotificationService getInstance() {
        if (notificationService == null) {
            notificationService = new NotificationServiceImpl();
        }
        return notificationService;
    }

    @Override
    public Map<String,Object> getTemplate(String actionType, Map<String,Object> reqContext) throws BaseException {

        Response response = notificationDao.getTemplateId(actionType,reqContext);
        if (null != response && MapUtils.isNotEmpty(response.getResult())) {
            List<Map<String, Object>> templateIdDetails =
                    (List<Map<String, Object>>) response.getResult().get(JsonKey.RESPONSE);
            if(CollectionUtils.isNotEmpty(templateIdDetails)){
                Map<String,Object> dbTemplateId = templateIdDetails.get(0);
                String templateId = (String) dbTemplateId.get(JsonKey.TEMPLATE_ID);
                Response responseObj = notificationDao.getTemplate(templateId, reqContext);
                if (null != responseObj && MapUtils.isNotEmpty(responseObj.getResult())) {
                    List<Map<String, Object>> templateDetails =
                            (List<Map<String, Object>>) responseObj.getResult().get(JsonKey.RESPONSE);
                    if(CollectionUtils.isNotEmpty(templateDetails)){
                        Map<String,Object> dbTemplate = templateDetails.get(0);
                        return dbTemplate;
                    }
                }
            }
        }
        return new HashMap<>();
    }

    @Override
    public void validateTemplate(Map<String, Object> paramObj, String templateSchema) throws BaseException {

        JSONObject jsonSchema = new JSONObject(templateSchema.toString());
        try {
            JSONObject jsonObject = new JSONObject(mapper.writeValueAsString(paramObj));
            Schema schema = SchemaLoader.load(jsonSchema);
            schema.validate(jsonObject);
        } catch (JsonProcessingException e) {
            logger.error("Error while validating template",e);
            throw new BaseException(IResponseMessage.INTERNAL_ERROR,e.getMessage(), ResponseCode.SERVER_ERROR.getCode());
        }

    }

    @Override
    public String transformTemplate(String templateData, Map<String, Object> paramObj) throws BaseException {
        VelocityEngine engine = new VelocityEngine();
        VelocityContext context =getContext(paramObj);
        Properties p = new Properties();
        p.setProperty("resource.loader", "class");
        p.setProperty(
                "class.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        StringWriter writer = null;
        String body = null;
        try {
            engine.init(p);
            writer = new StringWriter();
            Velocity.evaluate(context, writer, "log or null", templateData);
            return writer.toString();
        }catch (Exception ex){
            logger.error("Error while transforming template",ex);
            throw new BaseException(IResponseMessage.INTERNAL_ERROR,ex.getMessage(), ResponseCode.SERVER_ERROR.getCode());
        }
    }

    @Override
    public Response createNotificationFeed(List<NotificationFeed> feedList, Map<String,Object> reqContext) throws BaseException {
        Response response = notificationDao.createNotificationFeed(feedList,reqContext);
        return response;
    }

    @Override
    public Response createV1NotificationFeed(List<NotificationFeed> feedList, Map<String,Object> reqContext) throws BaseException, JsonProcessingException {
        Response response = notificationDao.createNotificationFeed(feedList,reqContext);
        return response;
    }

    @Override
    public Response deleteNotificationFeed(Map<String,List<String>> feedIdMap, Map<String, Object> reqContext) throws BaseException, JsonProcessingException {
        List<NotificationFeed> feeds = new ArrayList<>();
        for (Map.Entry<String,List<String>> feedItr:feedIdMap.entrySet()) {
            for (String feedId: feedItr.getValue()) {
                NotificationFeed feed = new NotificationFeed();
                feed.setId(feedId);
                feed.setUserId(feedItr.getKey());
                feeds.add(feed);
            }
        }
        if(CollectionUtils.isNotEmpty(feeds)) {
            return notificationDao.deleteUserFeed(feeds, reqContext);
        }else{
            throw new BaseException(IResponseMessage.INTERNAL_ERROR,IResponseMessage.Message.INTERNAL_ERROR, ResponseCode.SERVER_ERROR.getCode());
        }
    }

    @Override
    public Response mapV1V2Feed(List<NotificationFeed> newFeedList, List<NotificationFeed> oldFeedList, Map<String, Object> reqContext) {

        List<Map<String,Object>> mappedList = new ArrayList<>();
        for (NotificationFeed feed:newFeedList) {
             NotificationFeed oldFeed = oldFeedList.stream().filter(x->x.getUserId().equals(feed.getUserId())).findAny().orElse(null);
             if(null != oldFeed){
                 Map<String,Object> newFeedMap = new HashMap<>();
                 newFeedMap.put(JsonKey.ID,feed.getId());
                 newFeedMap.put(JsonKey.FEED_ID,oldFeed.getId());
                 mappedList.add(newFeedMap);
                 Map<String,Object> oldFeedMap = new HashMap<>();
                 oldFeedMap.put(JsonKey.ID,oldFeed.getId());
                 oldFeedMap.put(JsonKey.FEED_ID,feed.getId());
                 mappedList.add(oldFeedMap);
             }
        }
        return notificationDao.mapV1V2Feed(mappedList,reqContext);

    }

    @Override
    public List<Map<String, Object>> getFeedMap(List<String> feedIds,  Map<String,Object> reqContext) {
        Response response = notificationDao.getFeedMap(feedIds, reqContext);
        List<Map<String, Object>> feedMapLists = new ArrayList<>();
        if(null != response){
           feedMapLists = (List<Map<String, Object>>) response.getResult().get(JsonKey.RESPONSE);
        }
        return feedMapLists;
    }

    @Override
    public Response deleteNotificationFeedMap(List<String> feedIds, Map<String, Object> context) {
        return notificationDao.deleteUserFeedMap(feedIds,context);
    }


    @Override
    public List<Map<String, Object>> readNotificationFeed(String userId, Map<String,Object> reqContext) throws BaseException, IOException {
        logger.info(reqContext,"NotificationServiceImpl: readNotificationFeed: Started");
        Response response = notificationDao.readNotificationFeed(userId,reqContext);
        List<Map<String, Object>> notifications = new ArrayList<>();
        if (null != response && MapUtils.isNotEmpty(response.getResult())) {
            notifications = (List<Map<String, Object>>) response.getResult().get(JsonKey.RESPONSE);
            if(CollectionUtils.isNotEmpty(notifications)){
                Iterator<Map<String,Object>> notifyItr = notifications.iterator();
                while (notifyItr.hasNext()) {
                    Map<String,Object> notification = notifyItr.next();
                    if(JsonKey.V1.equals(notification.get(JsonKey.VERSION))){
                        notifyItr.remove();
                    }else{
                        String actionStr = (String) notification.get(JsonKey.ACTION);
                        Map<String,Object> actionData= null;
                        if(actionStr != null){
                            ObjectMapper mapper = new ObjectMapper();
                            actionData = mapper.readValue(actionStr,Map.class);
                        }
                        notification.put(JsonKey.ACTION,actionData);
                    }

                }
            }
        }
        logger.info(reqContext,"NotificationServiceImpl: readNotificationFeed: ended");

        return notifications;
    }

    @Override
    public List<Map<String, Object>> readV1NotificationFeed(String userId, Map<String,Object> reqContext) throws BaseException, IOException {

        Response response = notificationDao.readNotificationFeed(userId,reqContext);
        List<Map<String, Object>> notifications = new ArrayList<>();
        if (null != response && MapUtils.isNotEmpty(response.getResult())) {
            notifications = (List<Map<String, Object>>) response.getResult().get(JsonKey.RESPONSE);
            if(CollectionUtils.isNotEmpty(notifications)){
                Iterator<Map<String,Object>> notifyItr = notifications.iterator();
                while (notifyItr.hasNext()) {
                    Map<String,Object> notification = notifyItr.next();
                    if(!JsonKey.V1.equals(notification.get(JsonKey.VERSION))){
                       notifyItr.remove();
                    }else {
                        String actionStr = (String) notification.get(JsonKey.ACTION);
                        ObjectMapper mapper = new ObjectMapper();
                        Map actionData = mapper.readValue(actionStr, Map.class);
                        notification.put(JsonKey.DATA, actionData);
                        notification.remove(JsonKey.ACTION);
                    }
                }
            }
        }
        return notifications;
    }

    @Override
    public Response updateNotificationFeed( List<Map<String,Object>>  feeds, Map<String,Object> reqContext) throws BaseException {
        //Get mapping feed in other version format
        return notificationDao.updateNotificationFeed(feeds, reqContext);
    }

    private VelocityContext getContext(Map<String, Object> paramObj) {
        VelocityContext context = new VelocityContext();
        for (Map.Entry<String,Object> itr: paramObj.entrySet()){
            context.put(itr.getKey(),itr.getValue());
        }
        return context;
    }


    }
