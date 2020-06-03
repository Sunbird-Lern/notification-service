package controllers.notification;

import controllers.BaseControllerTest;
import controllers.TestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.mvc.Result;
import play.test.Helpers;

import javax.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class NotificationControllerTest extends BaseControllerTest {
  public static Application app;
  @Before
  public void setUp() {
    app = Helpers.fakeApplication();
  }

  @After
  public void tearDown() {
    app = null;
  }

  @Test
  public void sendNotification() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put("accept", "yes");
    Result result = testHelper.performTest("/v1/notification/send", "POST",reqMap,headerMap);
    assertTrue(testHelper.getResponseStatus(result) == Response.Status.OK.getStatusCode());
  }
}