package org.sunbird.dao;

import org.sunbird.common.exception.BaseException;
import org.sunbird.pojo.NotificationFeed;
import org.sunbird.common.response.Response;

import java.util.List;
import java.util.Map;

public interface NotificationDao {

    Response getTemplateId(String actionType, Map<String,Object> reqContext) throws BaseException;

    Response getTemplate(String templateId, Map<String,Object> reqContext) throws BaseException;

    Response createNotificationFeed(List<NotificationFeed> feed, Map<String,Object> reqContext) throws BaseException;

    Response readNotificationFeed(String userId, Map<String,Object> reqContext) throws BaseException;

    Response updateNotificationFeed(List<Map<String,Object>>  feed, Map<String,Object> reqContext) throws BaseException;

    Response deleteUserFeed(List<NotificationFeed> feeds,Map<String,Object> context) throws BaseException;

    Response mapV1V2Feed(List<Map<String, Object>> mappedList, Map<String, Object> reqContext);

    Response getFeedMap(List<String> feedIds, Map<String, Object> reqContext);

    Response deleteUserFeedMap(List<String> feedIds, Map<String, Object> context);
}
