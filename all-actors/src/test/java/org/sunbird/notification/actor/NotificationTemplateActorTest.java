package org.sunbird.notification.actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.JsonKey;
import org.sunbird.cassandra.CassandraOperation;
import org.sunbird.cassandraimpl.CassandraOperationImpl;
import org.sunbird.common.message.Localizer;
import org.sunbird.common.request.Request;
import org.sunbird.common.response.Response;
import org.sunbird.util.SystemConfigUtil;
import org.sunbird.utils.PropertiesCache;
import org.sunbird.utils.ServiceFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
@RunWith(PowerMockRunner.class)
@PrepareForTest({
        CassandraOperation.class,
        CassandraOperationImpl.class,
        ServiceFactory.class,
        Localizer.class,
        SystemConfigUtil.class,
        PropertiesCache.class
})
@PowerMockIgnore({"javax.management.*", "jdk.internal.reflect.*"})
public class NotificationTemplateActorTest extends BaseActorTest{

    private ObjectMapper mapper = new ObjectMapper();
    final  Props props = Props.create(NotificationTemplateActor.class);


    @Before
    public void setUp(){
        PowerMockito.mockStatic(Localizer.class);
        Mockito.when(Localizer.getInstance()).thenReturn(null);
        PowerMockito.mockStatic(ServiceFactory.class);
        CassandraOperation cassandraOperation ;
        cassandraOperation = mock(CassandraOperationImpl.class);
        when(ServiceFactory.getInstance()).thenReturn(cassandraOperation);
        when(cassandraOperation.getRecordsByProperty(Mockito.anyString(),Mockito.eq("action_template"),Mockito.anyString(),Mockito.anyString(),
                Mockito.anyMap())).thenReturn(getActionTemplateSuccess());

        when(cassandraOperation.getRecordsByProperty(Mockito.anyString(),Mockito.eq("notification_template"),Mockito.anyString(),Mockito.anyString(),
                Mockito.anyMap())).thenReturn(getTemplateSuccess());
        when(cassandraOperation.upsertRecord(Mockito.anyString(),Mockito.anyString(),Mockito.anyMap(),Mockito.anyMap())).thenReturn(getCassandraResponse());
        when(cassandraOperation.deleteRecord(Mockito.anyString(),Mockito.anyString(),Mockito.anyMap(),Mockito.anyMap())).thenReturn(getCassandraResponse());
        when(cassandraOperation.updateRecord(Mockito.anyString(),Mockito.anyString(),Mockito.anyMap(),Mockito.anyMap(),Mockito.anyMap())).thenReturn(getCassandraResponse());
        when(cassandraOperation.insertRecord(Mockito.anyString(),Mockito.anyString(),Mockito.anyMap(),Mockito.anyMap())).thenReturn(getCassandraResponse());



    }

    @Test
    public void readActionTemplateSuccess(){


        TestKit probe = new TestKit(system);
        ActorRef subject = system.actorOf(props);

        Request request = new Request();
        request.getRequest().put(JsonKey.ACTION,"member-added");
        Map<String,Object> context = new HashMap<>();
        request.setContext(context);
        request.setOperation("readActionTemplate");
        subject.tell(request, probe.getRef());
        Response res = probe.expectMsgClass(Duration.ofSeconds(80), Response.class);
        System.out.println(res.getResult());
        Assert.assertTrue(null != res && res.getResponseCode().getCode()==200);
    }

    @Test
    public void mapActionTemplateSuccess(){

        TestKit probe = new TestKit(system);
        ActorRef subject = system.actorOf(props);
        Request request = new Request();
        request.getRequest().put(JsonKey.ACTION,"member-added");
        request.getRequest().put(JsonKey.TEMPLATE_ID,"user-add");
        request.getRequest().put(JsonKey.TYPE,"feed");

        Map<String,Object> context = new HashMap<>();
        request.setContext(context);
        request.setOperation("mapActionTemplate");
        subject.tell(request, probe.getRef());
        Response res = probe.expectMsgClass(Duration.ofSeconds(80), Response.class);
        System.out.println(res.getResult());
        Assert.assertTrue(null != res && res.getResponseCode().getCode()==200);
    }

    @Test
    public void listTemplateSuccess(){

        TestKit probe = new TestKit(system);
        ActorRef subject = system.actorOf(props);
        Request request = new Request();
        Map<String,Object> context = new HashMap<>();
        request.setContext(context);
        request.setOperation("listTemplate");
        subject.tell(request, probe.getRef());
        Response res = probe.expectMsgClass(Duration.ofSeconds(80), Response.class);
        System.out.println(res.getResult());
        Assert.assertTrue(null != res && res.getResponseCode().getCode()==200);
    }

    @Test
    public void deleteTemplateSuccess(){
        TestKit probe = new TestKit(system);
        ActorRef subject = system.actorOf(props);
        Request request = new Request();
        request.getRequest().put(JsonKey.TEMPLATE_ID,"user-add");
        Map<String,Object> context = new HashMap<>();
        request.setContext(context);
        request.setOperation("deleteTemplate");
        subject.tell(request, probe.getRef());
        Response res = probe.expectMsgClass(Duration.ofSeconds(80), Response.class);
        System.out.println(res.getResult());
        Assert.assertTrue(null != res && res.getResponseCode().getCode()==200);
    }

    @Test
    public void updateTemplateSuccess(){

        TestKit probe = new TestKit(system);
        ActorRef subject = system.actorOf(props);
        Request request = new Request();
        request.getRequest().put(JsonKey.TEMPLATE_ID,"user-add");
        request.getRequest().put(JsonKey.TYPE,"feed");
        Map<String,Object> context = new HashMap<>();
        request.setContext(context);
        request.setOperation("updateTemplate");
        subject.tell(request, probe.getRef());
        Response res = probe.expectMsgClass(Duration.ofSeconds(80), Response.class);
        System.out.println(res.getResult());
        Assert.assertTrue(null != res && res.getResponseCode().getCode()==200);
    }

    @Test
    public void createTemplateSuccess(){
        TestKit probe = new TestKit(system);
        ActorRef subject = system.actorOf(props);
        Request request = new Request();
        request.getRequest().put(JsonKey.TEMPLATE_ID,"user-add");
        request.getRequest().put(JsonKey.TYPE,"feed");
        request.getRequest().put(JsonKey.DATA,"{\"title\":\"${param1} exit\"}");
        request.getRequest().put(JsonKey.TEMPLATE_SCHEMA,"\"{\"schema}\":\"test_schema\"}\"");
        Map<String,Object> context = new HashMap<>();
        request.setContext(context);
        request.setOperation("createTemplate");
        subject.tell(request, probe.getRef());
        Response res = probe.expectMsgClass(Duration.ofSeconds(80), Response.class);
        System.out.println(res.getResult());
        Assert.assertTrue(null != res && res.getResponseCode().getCode()==200);
    }

    private Response getTemplateSuccess() {
        List<Map<String,Object>> templateDetails = new ArrayList<>();
        Map<String,Object> data = new HashMap<>();
        data.put(JsonKey.TEMPLATE_ID,"user-exit");
        data.put(JsonKey.TYPE,"JSON");
        data.put(JsonKey.TEMPLATE_SCHEMA,"{\"schema}\":\"test_schema\"}");
        data.put(JsonKey.DATA,"{\"title\":\"${param1} is exit\"}");
        templateDetails.add(data);
        Map<String, Object> result = new HashMap<>();
        result.put(org.sunbird.common.util.JsonKey.RESPONSE, templateDetails);
        Response response = new Response();
        response.putAll(result);
        return response;
    }

    private Response getActionTemplateSuccess() {
        List<Map<String,Object>> templateIdDetails = new ArrayList<>();
        Map<String,Object> data = new HashMap<>();
        data.put(JsonKey.ACTION,"member-added");
        data.put(JsonKey.TEMPLATE_ID,"user-exit");
        data.put(JsonKey.TYPE,"FEED");
        templateIdDetails.add(data);
        Map<String, Object> result = new HashMap<>();
        result.put(org.sunbird.common.util.JsonKey.RESPONSE, templateIdDetails);
        Response response = new Response();
        response.putAll(result);
        return response;
    }

}
