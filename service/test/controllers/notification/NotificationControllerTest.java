package controllers.notification;

import controllers.BaseControllerTest;
import controllers.DummyActor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sunbird.common.util.JsonKey;
import play.Application;
import play.mvc.Result;

import javax.ws.rs.core.Response;

import java.util.*;

import static org.junit.Assert.*;

public class NotificationControllerTest extends BaseControllerTest {
  public static Application app;
  @Before
  public void setUp() {
    setup(DummyActor.class);
  }

  @After
  public void tearDown() {
    app = null;
  }

  @Test
  public void sendNotification() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put("accept", "yes");
    Result result = performTest("/v1/notification/send", "POST",reqMap);
    assertTrue(getResponseStatus(result) == Response.Status.OK.getStatusCode());
  }

  @Test
  public void sendV2Notification() {
    Map<String, Object> request = getV2NotificationRequest();
    Result result = performTest("/v2/notification/send", "POST",request);
    assertTrue(getResponseStatus(result) == Response.Status.OK.getStatusCode());
  }

  @Test
  public void sendSyncNotification() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put("accept", "yes");
    Result result = performTest("/v1/notification/send/sync", "POST",reqMap);
    assertTrue(getResponseStatus(result) == Response.Status.OK.getStatusCode());
  }

  @Test
  public void sendV1Notification() {
    Map<String, Object> request = getV1NotificationRequest();
    Result result = performTest("/private/v2/notification/send", "POST",request);
    assertTrue(getResponseStatus(result) == Response.Status.OK.getStatusCode());
  }
  
  @Test
  public void sendOTPNotification() {
    Map<String, Object> request = getV1NotificationRequest();
    Result result = performTest("/v1/notification/otp/verify", "POST",request);
    assertTrue(getResponseStatus(result) == Response.Status.OK.getStatusCode());
  }

  @Test
  public void readFeedNotification() {
    Result result = performTest("/v1/notification/feed/read/12345", "GET",null);
    assertTrue(getResponseStatus(result) == Response.Status.OK.getStatusCode());
  }

  @Test
  public void readV1FeedNotification() {
    Result result = performTest("/private/v1/notification/feed/read/12345", "GET",null);
    assertTrue(getResponseStatus(result) == Response.Status.OK.getStatusCode());
  }

  @Test
  public void updateFeedNotification() {
    Map<String,Object> req = new HashMap<>();
    req.put(JsonKey.IDS,Arrays.asList("12323423232"));
    req.put(JsonKey.STATUS,"read");
    Map<String,Object> reqObj = new HashMap<>();
    reqObj.put(JsonKey.REQUEST,req);
    Result result = performTest("/v1/notification/feed/update", "PATCH",reqObj);
    assertTrue(getResponseStatus(result) == Response.Status.OK.getStatusCode());
  }

  @Test
  public void updateV1FeedNotification() {
    Map<String,Object> req = new HashMap<>();
    req.put(JsonKey.IDS,Arrays.asList("12323423232"));
    req.put(JsonKey.STATUS,"read");
    Map<String,Object> reqObj = new HashMap<>();
    reqObj.put(JsonKey.REQUEST,req);
    Result result = performTest("/private/v1/notification/feed/update", "PATCH",reqObj);
    assertTrue(getResponseStatus(result) == Response.Status.OK.getStatusCode());
  }

  @Test
  public void deleteFeedNotification() {
    Map<String,Object> req = new HashMap<>();
    req.put(JsonKey.IDS,Arrays.asList("12323423232"));
    req.put(JsonKey.USER_ID,"12313213");
    req.put(JsonKey.CATEGORY,"groups");
    Map<String,Object> reqObj = new HashMap<>();
    reqObj.put(JsonKey.REQUEST,req);
    Result result = performTest("/v1/notification/feed/delete", "POST",reqObj);
    assertTrue(getResponseStatus(result) == Response.Status.OK.getStatusCode());
  }

  @Test
  public void deleteV1FeedNotification() {
    Map<String,Object> req = new HashMap<>();
    req.put(JsonKey.IDS,Arrays.asList("12323423232"));
    req.put(JsonKey.USER_ID,"12313213");
    req.put(JsonKey.CATEGORY,"groups");
    Map<String,Object> reqObj = new HashMap<>();
    reqObj.put(JsonKey.REQUEST,req);
    Result result = performTest("/private/v1/notification/feed/delete", "POST",reqObj);
    assertTrue(getResponseStatus(result) == Response.Status.OK.getStatusCode());
  }

  @Test
  public void deleteFeedNotificationFailed() {
    Map<String,Object> req = new HashMap<>();
    req.put(JsonKey.IDS,Arrays.asList("12323423232"));
    req.put(JsonKey.CATEGORY,"groups");
    Map<String,Object> reqObj = new HashMap<>();
    reqObj.put(JsonKey.REQUEST,req);
    Result result = performTest("/private/v1/notification/feed/delete", "POST",reqObj);
    assertFalse(getResponseStatus(result) == Response.Status.OK.getStatusCode());

  }


  private Map<String, Object> getV2NotificationRequest() {
    Map<String,Object> request = new HashMap<>();
    Map<String, Object> reqMap = new HashMap<>();
    Map<String,Object> notification = new HashMap<>();
    Map<String,Object> action = new HashMap<>();
    Map<String,Object> template = new HashMap<>();
    template.put(JsonKey.PARAMS,new HashMap<>());
    action.put(JsonKey.TEMPLATE,template);
    Map<String,Object> createdBy = new HashMap<>();
    createdBy.put(JsonKey.ID,"12354");
    createdBy.put(JsonKey.TYPE,JsonKey.USER);
    action.put(JsonKey.CREATED_BY,createdBy);
    action.put(JsonKey.ADDITIONAL_INFO,new HashMap<>());
    action.put(JsonKey.TYPE,"add-member");
    action.put(JsonKey.CATEGORY,"groups");
    notification.put(JsonKey.ACTION,action);
    notification.put(JsonKey.IDS,Arrays.asList("1234"));
    notification.put(JsonKey.TYPE,"feed");
    notification.put("priority",1);
    reqMap.put(JsonKey.NOTIFICATIONS,Arrays.asList(notification));
    request.put(JsonKey.REQUEST,reqMap);
    return request;
  }

  private Map<String, Object> getV1NotificationRequest() {
    Map<String,Object> request = new HashMap<>();
    Map<String, Object> reqMap = new HashMap<>();
    Map<String,Object> notification = new HashMap<>();
    Map<String,Object> data = new HashMap<>();
    data.put("actionData",new HashMap<>());
    notification.put(JsonKey.DATA,data);
    notification.put(JsonKey.IDS,Arrays.asList("1234"));
    notification.put(JsonKey.TYPE,"feed");
    notification.put("priority",1);
    reqMap.put(JsonKey.NOTIFICATIONS,Arrays.asList(notification));
    request.put(JsonKey.REQUEST,reqMap);
    return request;
  }

}