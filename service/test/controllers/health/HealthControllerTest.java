package controllers.health;

import akka.actor.Props;
import controllers.BaseControllerTest;
import controllers.TestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sunbird.health.actor.HealthActor;
import play.Application;
import play.mvc.Result;
import play.test.Helpers;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class HealthControllerTest extends BaseControllerTest {
  TestHelper testHelper;
  public static Application app;
  public static Map<String, String[]> headerMap;
  private static final Props props = Props.create(HealthActor.class);

  @Before
  public void setUp(){
    testHelper = new TestHelper();
    app = Helpers.fakeApplication();
    Helpers.start(app);
    headerMap = testHelper.getHeaderMap();
  }

  @After
  public void tearDown(){
    headerMap = null;
    app = null;
    testHelper = null;
  }

  //@Test
  public void testGetHealthSuccess() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put("accept", "yes");
    Result result = testHelper.performTest("/health", "GET", reqMap, headerMap);
    assertTrue(testHelper.getResponseStatus(result) == Response.Status.OK.getStatusCode());
  }
  @Test
  public void testGetHealthFailure() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put("accept", "yes");
    Result result = testHelper.performTest("/health", "POST", reqMap, headerMap);
    assertTrue(testHelper.getResponseStatus(result) == Response.Status.NOT_FOUND.getStatusCode());
  }


  @Test
  public void testGetServiceHealthSuccess() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put("accept", "yes");
    Result result = testHelper.performTest("/service/health", "GET", reqMap, headerMap);
    assertTrue(testHelper.getResponseStatus(result) == Response.Status.OK.getStatusCode());
  }
  @Test
  public void testGetServiceHealthFailure() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put("accept", "yes");
    Result result = testHelper.performTest("/user-service/health", "POST", reqMap, headerMap);
    assertTrue(testHelper.getResponseStatus(result) == Response.Status.NOT_FOUND.getStatusCode());
  }


}