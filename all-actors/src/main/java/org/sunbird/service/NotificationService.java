package org.sunbird.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.sunbird.common.exception.BaseException;
import org.sunbird.pojo.NotificationFeed;
import org.sunbird.pojo.NotificationV2Request;
import org.sunbird.common.response.Response;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface NotificationService {

    Map<String,Object> getTemplate(String actionType, Map<String,Object> reqContext) throws BaseException;

    void validateTemplate(Map<String,Object> paramObj, String templateSchema) throws BaseException;

    String transformTemplate(String templateData, Map<String,Object> paramObj) throws BaseException;

    Response createNotificationFeed(List<NotificationFeed> feedList, Map<String,Object> reqContext) throws BaseException;

    List<Map<String, Object>> readNotificationFeed(String userId, Map<String,Object> reqContext) throws BaseException, IOException;

    Response updateNotificationFeed( List<Map<String,Object>>  feeds,Map<String,Object> reqContext) throws BaseException;

    List<Map<String, Object>> readV1NotificationFeed(String userId, Map<String,Object> reqContext) throws BaseException, IOException;

    Response createV1NotificationFeed(List<NotificationFeed> feedList, Map<String,Object> reqContext) throws BaseException, JsonProcessingException;

    Response deleteNotificationFeed(Map<String,List<String>> feedIdMap, Map<String,Object> reqContext) throws BaseException, JsonProcessingException;

    Response mapV1V2Feed(List<NotificationFeed> newFeedList, List<NotificationFeed> oldFeedList, Map<String, Object> reqContext);

    List<Map<String, Object>> getFeedMap(List<String> strings,  Map<String,Object> reqContext);

    Response deleteNotificationFeedMap(List<String> feedIds, Map<String, Object> context);
}
