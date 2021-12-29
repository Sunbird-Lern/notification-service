package controllers.notification;

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

public class NotificationTemplateControllerTest extends BaseControllerTest {
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
    public void createTemplate(){
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("accept", "yes");
        Result result = performTest("/v1/notification/template/create", "POST",reqMap);
        assertTrue(getResponseStatus(result) == Response.Status.OK.getStatusCode());

    }

    @Test
    public void updateTemplate(){
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("accept", "yes");
        Result result = performTest("/v1/notification/template/update", "PATCH",reqMap);
        assertTrue(getResponseStatus(result) == Response.Status.OK.getStatusCode());

    }

    @Test
    public void listTemplate(){
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("accept", "yes");
        Result result = performTest("/v1/notification/template/list", "GET",reqMap);
        assertTrue(getResponseStatus(result) == Response.Status.OK.getStatusCode());

    }

    @Test
    public void deleteTemplate(){
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("accept", "yes");
        Result result = performTest("/v1/notification/template/delete", "POST",reqMap);
        assertTrue(getResponseStatus(result) == Response.Status.OK.getStatusCode());

    }


}
