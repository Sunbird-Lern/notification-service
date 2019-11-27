package controllers.health;

import static org.junit.Assert.*;

import controllers.BaseControllerTest;
import controllers.TestHelper;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.Application;
import play.mvc.Result;
import play.test.Helpers;

public class HealthControllerTest extends BaseControllerTest {
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
  public void testGetHealthSuccess() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put("accept", "yes");
    Result result = testHelper.performTest("/health", "GET", reqMap, headerMap, app);
    assertTrue(testHelper.getResponseStatus(result) == Response.Status.OK.getStatusCode());
  }

  @Test
  public void testGetHealthFailure() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put("accept", "yes");
    Result result = testHelper.performTest("/health", "POST", reqMap, headerMap, app);
    assertTrue(testHelper.getResponseStatus(result) == Response.Status.NOT_FOUND.getStatusCode());
  }

  @Test
  public void testCompleteServiceHealthSuccess() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put("accept", "yes");
    Result result = testHelper.performTest("/service/health", "GET", reqMap, headerMap, app);
    assertTrue(testHelper.getResponseStatus(result) == Response.Status.OK.getStatusCode());
  }

  @Test
  public void testCompleteServiceHealthFailure() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put("accept", "yes");
    Result result = testHelper.performTest("/user-service/health", "GET", reqMap, headerMap, app);
    assertTrue(testHelper.getResponseStatus(result) == Response.Status.BAD_REQUEST.getStatusCode());
  }
}
