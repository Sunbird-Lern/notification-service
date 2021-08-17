package controllers.notification;

import controllers.BaseControllerTest;
import controllers.DummyActor;
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


}