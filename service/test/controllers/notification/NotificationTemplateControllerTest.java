package controllers.notification;

import controllers.BaseControllerTest;
import controllers.DummyActor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sunbird.JsonKey;
import org.sunbird.common.request.Request;
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
        reqMap.put(JsonKey.TEMPLATE_ID,"user-add");
        reqMap.put(JsonKey.TYPE,"feed");
        reqMap.put(JsonKey.DATA,"{\"title\":\"${param1} exit\"}");
        reqMap.put(JsonKey.TEMPLATE_SCHEMA,"\"{\"schema}\":\"test_schema\"}\"");
        Map<String,Object> request = new HashMap<>();
        request.put("accept", "yes");
        request.put(JsonKey.REQUEST,reqMap);
        Result result = performTest("/v1/notification/template/create", "POST",request);
        assertTrue(getResponseStatus(result) == Response.Status.OK.getStatusCode());

    }

    @Test
    public void updateTemplate(){
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put(JsonKey.TEMPLATE_ID,"user-add");
        Map<String,Object> request = new HashMap<>();
        request.put("accept", "yes");
        request.put(JsonKey.REQUEST,reqMap);
        Result result = performTest("/v1/notification/template/update", "PATCH",request);
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
        reqMap.put(JsonKey.TEMPLATE_ID,"user-add");
        Map<String,Object> request = new HashMap<>();
        request.put("accept", "yes");
        request.put(JsonKey.REQUEST,reqMap);
        Result result = performTest("/v1/notification/template/delete", "POST",request);
        assertTrue(getResponseStatus(result) == Response.Status.OK.getStatusCode());

    }


}
