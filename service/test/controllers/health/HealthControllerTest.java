package controllers.health;

import controllers.BaseControllerTest;
import controllers.DummyActor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.mvc.Result;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class HealthControllerTest extends BaseControllerTest {
  public static Application app;
  @Before
  public void setUp(){
    setup(DummyActor.class);
  }

  @After
  public void tearDown(){
    app = null;
  }

  //@Test
  public void testGetHealthSuccess() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put("accept", "yes");
    Result result = performTest("/health", "GET", reqMap);
    assertTrue(getResponseStatus(result) == Response.Status.OK.getStatusCode());
  }
  @Test
  public void testGetHealthFailure() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put("accept", "yes");
    Result result = performTest("/health", "POST", reqMap);
    assertTrue(getResponseStatus(result) == Response.Status.NOT_FOUND.getStatusCode());
  }


  @Test
  public void testGetServiceHealthSuccess() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put("accept", "yes");
    Result result = performTest("/service/health", "GET", reqMap);
    assertTrue(getResponseStatus(result) == Response.Status.OK.getStatusCode());
  }
  @Test
  public void testGetServiceHealthFailure() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put("accept", "yes");
    Result result = performTest("/user-service/health", "POST", reqMap);
    assertTrue(getResponseStatus(result) == Response.Status.NOT_FOUND.getStatusCode());
  }


}