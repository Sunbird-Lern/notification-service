package org.sunbird.notification.actor;

import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.sunbird.Application;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.response.Response;
import org.sunbird.common.util.JsonKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PrepareForTest({Application.class})
public abstract class BaseActorTest {

    static Map<String, Object> headerMap = new HashMap<>();
    static ActorSystem system;

    @BeforeClass
    public  static void setup() {
        system = ActorSystem.create("system");
        setReqId();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    private static void setReqId() {
        List<String> reqIds = new ArrayList<>();
        reqIds.add("71ef3311-ac58-49a1-872b-7cf28159de83");
        headerMap.put(JsonKey.REQUEST_MESSAGE_ID, reqIds);
    }

    public static Response getCassandraResponse() {
        Response response = new Response();
        Map<String, Object> result = new HashMap<>();
        result.put(JsonKey.RESPONSE, ResponseCode.OK.getCode());
        response.putAll(result);
        return response;
    }


    protected Response getNotificationFeedResponse() {
        Response response = new Response();
        Map<String, Object> result = new HashMap<>();
        List<Map<String,Object>> notificationFeeds = new ArrayList<>();

        Map<String,Object> notificationV1Feed = new HashMap<>();
        notificationV1Feed.put(org.sunbird.JsonKey.USER_ID,"123456");
        notificationV1Feed.put(org.sunbird.JsonKey.ID,"13213213123131");
        notificationV1Feed.put(org.sunbird.JsonKey.PRIORITY,1);
        notificationV1Feed.put(org.sunbird.JsonKey.STATUS,"unread");
        notificationV1Feed.put(org.sunbird.JsonKey.CATEGORY,"Groups");
        notificationV1Feed.put(org.sunbird.JsonKey.ACTION,"{\"actionData\":{\"title\":\"test is game\",\"description\":\"This is desc\",\"contentUrl\":\"http://www.sunbird.org/test\"}}");
        notificationV1Feed.put(org.sunbird.JsonKey.VERSION,"v1");

        Map<String,Object> notificationV2Feed = new HashMap<>();
        notificationV1Feed.put(org.sunbird.JsonKey.USER_ID,"123456");
        notificationV1Feed.put(org.sunbird.JsonKey.ID,"13213213123131");
        notificationV1Feed.put(org.sunbird.JsonKey.PRIORITY,1);
        notificationV1Feed.put(org.sunbird.JsonKey.STATUS,"unread");
        notificationV1Feed.put(org.sunbird.JsonKey.CATEGORY,"Groups");
        notificationV1Feed.put(org.sunbird.JsonKey.ACTION,"{\"type\":\"add-member\",\"category\":\"groups\",\"template\":{\"data\":\"{\\\"title\\\":\\\"youhavebeenadded\\\"}\",\"type\":\"JSON\"},\"createdBy\":{\"id\":\"12321323\"},\"additionalInfo\":{\"identifier\":\"1323213\"}}");
        notificationFeeds.add(notificationV1Feed);
        notificationFeeds.add(notificationV2Feed);
        result.put(org.sunbird.JsonKey.RESPONSE,notificationFeeds);
        response.putAll(result);
        return response;
    }

    protected Response getFeedMapList() {
        Response response = new Response();
        Map<String, Object> result = new HashMap<>();
        List<Map<String,Object>> feedMapList = new ArrayList<>();
        Map<String,Object> feedMap = new HashMap<>();
        feedMap.put(JsonKey.ID,"123213213");
        feedMap.put(org.sunbird.JsonKey.FEED_ID,"12323123");
        feedMapList.add(feedMap);
        result.put(JsonKey.RESPONSE,feedMapList);
        response.putAll(result);
        return response;
    }
}
