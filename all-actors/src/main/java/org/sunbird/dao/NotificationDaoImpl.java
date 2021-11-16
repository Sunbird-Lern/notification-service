package org.sunbird.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sunbird.JsonKey;
import org.sunbird.cassandra.CassandraOperation;
import org.sunbird.common.Constants;
import org.sunbird.common.exception.BaseException;
import org.sunbird.utils.ServiceFactory;
import org.sunbird.pojo.NotificationFeed;
import org.sunbird.common.response.Response;

import java.util.*;

public class NotificationDaoImpl implements NotificationDao{
    private static final String NOTIFICATION_FEED = "notification_feed";
    private static final String NOTIFICATION_ACTION_TEMPLATE = "action_template";
    private static final String NOTIFICATION_TEMPLATE = "notification_template";
    private static final String KEY_SPACE_NAME = "sunbird_notifications";
    private static final String FEED_VERSION_MAP = "feed_version_map";

    private CassandraOperation cassandraOperation = ServiceFactory.getInstance();
    private ObjectMapper mapper = new ObjectMapper();

    private static NotificationDao notificationDao = null;

    public static NotificationDao getInstance() {
        if (notificationDao == null) {
            notificationDao = new NotificationDaoImpl();
        }
        return notificationDao;
    }

    @Override
    public Response getTemplate(String templateId, Map<String,Object> reqContext) throws BaseException {


        return cassandraOperation.getRecordsByProperty(KEY_SPACE_NAME,NOTIFICATION_TEMPLATE,JsonKey.TEMPLATE_ID,templateId,reqContext);

    }

    @Override
    public Response getTemplateId(String actionType, Map<String,Object> reqContext) throws BaseException {

        return cassandraOperation.getRecordsByProperty(KEY_SPACE_NAME,NOTIFICATION_ACTION_TEMPLATE,JsonKey.ACTION,actionType,reqContext);
    }

    @Override
    public Response createNotificationFeed(List<NotificationFeed> feeds, Map<String,Object> reqContext) throws BaseException {
        List<Map<String, Object>> feedList =
                mapper.convertValue(feeds, new TypeReference<List<Map<String, Object>>>() {});
        return cassandraOperation.batchInsert(KEY_SPACE_NAME, NOTIFICATION_FEED, feedList, reqContext);

    }

    @Override
    public Response readNotificationFeed(String userId, Map<String,Object> reqContext) throws BaseException {
        Map<String, Object> reqMap = new WeakHashMap<>(2);
        reqMap.put(JsonKey.USER_ID, userId);
        return cassandraOperation.getRecordById(KEY_SPACE_NAME,NOTIFICATION_FEED,reqMap,reqContext);
    }


    @Override
    public Response updateNotificationFeed( List<Map<String,Object>> feeds, Map<String,Object> reqContext) throws BaseException {
        List<Map<String, Map<String, Object>>> properties = new ArrayList<>();
        for(Map<String,Object> feedMap : feeds){
            Map<String,Map<String,Object>> keysMap = new HashMap<>();
            Map<String, Object> primaryKeyMap = new HashMap<>();
            Map<String, Object> nonPrimaryKeyMap = new HashMap<>();

            for(Map.Entry<String,Object> feedEntry: feedMap.entrySet()){
                if(JsonKey.ID.equals(feedEntry.getKey()) || JsonKey.USER_ID.equals(feedEntry.getKey())){
                    primaryKeyMap.put(feedEntry.getKey(),feedEntry.getValue());
                }else{
                    nonPrimaryKeyMap.put(feedEntry.getKey(),feedEntry.getValue());
                }
            }
            keysMap.put(Constants.PRIMARY_KEY,primaryKeyMap);
            keysMap.put(Constants.NON_PRIMARY_KEY,nonPrimaryKeyMap);
            properties.add(keysMap);
        }
        return  cassandraOperation.batchUpdate(KEY_SPACE_NAME, NOTIFICATION_FEED, properties,reqContext);

    }

    @Override
    public Response deleteUserFeed(List<NotificationFeed> feeds, Map<String,Object> context) throws BaseException {
        List<Map<String,Object>> properties = new ArrayList<>();
        for (NotificationFeed feed : feeds) {
            Map<String,Object> map = new HashMap<>();
            map.put(JsonKey.ID,feed.getId());
            map.put(JsonKey.USER_ID,feed.getUserId());
            properties.add(map);
        }
       return cassandraOperation.batchDelete(KEY_SPACE_NAME,NOTIFICATION_FEED, properties, context);
    }

    @Override
    public Response mapV1V2Feed(List<Map<String, Object>> mappedList, Map<String, Object> reqContext) {
        return cassandraOperation.batchInsert(KEY_SPACE_NAME, FEED_VERSION_MAP, mappedList, reqContext);

    }

    @Override
    public Response getFeedMap(List<String> feedIds, Map<String, Object> reqContext) {
        return cassandraOperation.getRecordsByPrimaryKeys(KEY_SPACE_NAME,FEED_VERSION_MAP,feedIds,JsonKey.ID,reqContext);
    }

    @Override
    public Response deleteUserFeedMap(List<String> feedIds, Map<String, Object> context) {
        List<Map<String,Object>> properties = new ArrayList<>();
        for (String feedId : feedIds) {
            Map<String,Object> map = new HashMap<>();
            map.put(JsonKey.ID,feedId);
            properties.add(map);
        }
        return cassandraOperation.batchDelete(KEY_SPACE_NAME,FEED_VERSION_MAP, properties, context);
    }
}
