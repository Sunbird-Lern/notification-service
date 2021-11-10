package org.sunbird.notification.actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.cassandra.CassandraOperation;
import org.sunbird.cassandraimpl.CassandraOperationImpl;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.Localizer;
import org.sunbird.common.request.Request;
import org.sunbird.common.response.Response;
import org.sunbird.common.util.JsonKey;
import org.sunbird.notification.email.Email;

import org.sunbird.util.SystemConfigUtil;
import org.sunbird.utils.PropertiesCache;
import org.sunbird.utils.ServiceFactory;

import java.time.Duration;
import java.util.*;

import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        CassandraOperation.class,
        CassandraOperationImpl.class,
        ServiceFactory.class,
        Localizer.class,
        SystemConfigUtil.class,
        PropertiesCache.class,
        Email.class,
        HttpClients.class

})
@PowerMockIgnore({"javax.management.*", "jdk.internal.reflect.*"})
public class CreateNotificationActorTest extends BaseActorTest{

    public  final Props props = Props.create(CreateNotificationActor.class);

    public  PropertiesCache propertiesCache;
    @Before
    public void setUp() throws Exception {

        PowerMockito.mockStatic(Localizer.class);
        Mockito.when(Localizer.getInstance()).thenReturn(null);
        PowerMockito.mockStatic(SystemConfigUtil.class);
        PowerMockito.mockStatic(PropertiesCache.class);
        propertiesCache = Mockito.mock(PropertiesCache.class);
        Mockito.when(PropertiesCache.getInstance()).thenReturn(propertiesCache);
        when(propertiesCache.getProperty(org.sunbird.JsonKey.NOTIFICATION_CATEGORY_TYPE_CONFIG)).thenReturn("certificateUpload,add-member");
        when(propertiesCache.getProperty(org.sunbird.JsonKey.VERSION_SUPPORT_CONFIG_ENABLE)).thenReturn("true");
        when(propertiesCache.getProperty(org.sunbird.JsonKey.FEED_LIMIT)).thenReturn("1");

    }

    @Test
    public void testCreateNotificationSuccess(){

        TestKit probe = new TestKit(system);
        ActorRef subject = system.actorOf(props);
        try {
            CassandraOperation cassandraOperation;
            PowerMockito.mockStatic(ServiceFactory.class);
            cassandraOperation = mock(CassandraOperationImpl.class);
            when(ServiceFactory.getInstance()).thenReturn(cassandraOperation);
            when(cassandraOperation.getRecordsByProperty(
                    Mockito.eq(JsonKey.SUNBIRD_NOTIFICATIONS),
                    Mockito.eq("action_template"),
                    Mockito.eq(JsonKey.ACTION),
                    Mockito.anyString(),
                    Mockito.any()))
                    .thenReturn(getAddActionTemplate());
            when(cassandraOperation.getRecordsByProperty(
                    Mockito.eq(JsonKey.SUNBIRD_NOTIFICATIONS),
                    Mockito.eq("notification_template"),
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.any()))
                    .thenReturn(getNotificationTemplate());
            when(cassandraOperation.batchInsert(
                    Mockito.anyString(), Mockito.anyString(), Mockito.anyList(),Mockito.any()))
                    .thenReturn(getCassandraResponse());
            when(cassandraOperation.getRecordById(
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.anyMap(),
                    Mockito.anyMap()))
                    .thenReturn(getNotificationFeedResponse());

            when(cassandraOperation.getRecordsByProperty(
                    Mockito.eq(JsonKey.SUNBIRD_NOTIFICATIONS),
                    Mockito.eq("feed_version_map"),
                    Mockito.anyString(),
                    Mockito.anyList(),
                    Mockito.any()))
                    .thenReturn(getFeedMapList());
            when(cassandraOperation.batchDelete(
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.anyList(),
                    Mockito.anyMap()))
                    .thenReturn(getCassandraResponse());
        }catch (BaseException be) {
            Assert.assertTrue(false);
        }

        Request request = getV2NotificationRequest();
        subject.tell(request, probe.getRef());
        Response res = probe.expectMsgClass(Duration.ofSeconds(80), Response.class);
        System.out.println(res.getResult());
        Assert.assertTrue(null != res && res.getResponseCode().getCode()==200);

    }

    @Test
    public void testCreateV1NotificationSuccess(){

        TestKit probe = new TestKit(system);
        ActorRef subject = system.actorOf(props);
        try {
            CassandraOperation cassandraOperation;
            PowerMockito.mockStatic(ServiceFactory.class);
            cassandraOperation = mock(CassandraOperationImpl.class);
            when(ServiceFactory.getInstance()).thenReturn(cassandraOperation);
            when(cassandraOperation.getRecordsByProperty(
                    Mockito.eq(JsonKey.SUNBIRD_NOTIFICATIONS),
                    Mockito.eq("action_template"),
                    Mockito.eq(JsonKey.ACTION),
                    Mockito.anyString(),
                    Mockito.any()))
                    .thenReturn(getAddActionTemplate());
            when(cassandraOperation.getRecordsByProperty(
                    Mockito.eq(JsonKey.SUNBIRD_NOTIFICATIONS),
                    Mockito.eq("notification_template"),
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.any()))
                    .thenReturn(getNotificationTemplate());
            when(cassandraOperation.batchInsert(
                    Mockito.anyString(), Mockito.anyString(), Mockito.anyList(),Mockito.any()))
                    .thenReturn(getCassandraResponse());
            when(cassandraOperation.getRecordById(
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.anyMap(),
                    Mockito.anyMap()))
                    .thenReturn(getNotificationFeedResponse());

            when(cassandraOperation.getRecordsByProperty(
                    Mockito.eq(JsonKey.SUNBIRD_NOTIFICATIONS),
                    Mockito.eq("feed_version_map"),
                    Mockito.anyString(),
                    Mockito.anyList(),
                    Mockito.any()))
                    .thenReturn(getFeedMapList());
            when(cassandraOperation.batchDelete(
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.anyList(),
                    Mockito.anyMap()))
                    .thenReturn(getCassandraResponse());

        }catch (BaseException be) {
            Assert.assertTrue(false);
        }

        Request request = getV1NotificationRequest();
        subject.tell(request, probe.getRef());
        Response res = probe.expectMsgClass(Duration.ofSeconds(80), Response.class);
        System.out.println(res.getResult());
        Assert.assertTrue(null != res && res.getResponseCode().getCode()==200);

    }

    @Test
    public void testCreateV2NotificationParamMissing(){

        TestKit probe = new TestKit(system);
        ActorRef subject = system.actorOf(props);
        try {
            CassandraOperation cassandraOperation;
            PowerMockito.mockStatic(ServiceFactory.class);
            cassandraOperation = mock(CassandraOperationImpl.class);
            when(ServiceFactory.getInstance()).thenReturn(cassandraOperation);
            when(cassandraOperation.getRecordsByProperty(
                    Mockito.eq(JsonKey.SUNBIRD_NOTIFICATIONS),
                    Mockito.eq("action_template"),
                    Mockito.eq(JsonKey.ACTION),
                    Mockito.anyString(),
                    Mockito.any()))
                    .thenReturn(getAddActionTemplate());
            when(cassandraOperation.getRecordsByProperty(
                    Mockito.eq(JsonKey.SUNBIRD_NOTIFICATIONS),
                    Mockito.eq("notification_template"),
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.any()))
                    .thenReturn(getNotificationTemplate());
            when(cassandraOperation.batchInsert(
                    Mockito.anyString(), Mockito.anyString(), Mockito.anyList(),Mockito.any()))
                    .thenReturn(getCassandraResponse());
            when(cassandraOperation.getRecordById(
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.anyMap(),
                    Mockito.anyMap()))
                    .thenReturn(getNotificationFeedResponse());

            when(cassandraOperation.getRecordsByProperty(
                    Mockito.eq(JsonKey.SUNBIRD_NOTIFICATIONS),
                    Mockito.eq("feed_version_map"),
                    Mockito.anyString(),
                    Mockito.anyList(),
                    Mockito.any()))
                    .thenReturn(getFeedMapList());
            when(cassandraOperation.batchDelete(
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.anyList(),
                    Mockito.anyMap()))
                    .thenReturn(getCassandraResponse());

        }catch (BaseException be) {
            Assert.assertTrue(false);
        }

        Request request = getV2NotificationRequestWithParamMissing();
        subject.tell(request, probe.getRef());
        BaseException ex = probe.expectMsgClass(Duration.ofSeconds(80), BaseException.class);
        Assert.assertTrue(ex.getResponseCode()==400);

    }

   
    @Test
    public void testCreateV2NotificationTemplateTypeMissing(){

        TestKit probe = new TestKit(system);
        ActorRef subject = system.actorOf(props);
        try {
            CassandraOperation cassandraOperation;
            PowerMockito.mockStatic(ServiceFactory.class);
            cassandraOperation = mock(CassandraOperationImpl.class);
            when(ServiceFactory.getInstance()).thenReturn(cassandraOperation);
            when(cassandraOperation.getRecordsByProperty(
                    Mockito.eq(JsonKey.SUNBIRD_NOTIFICATIONS),
                    Mockito.eq("action_template"),
                    Mockito.eq(JsonKey.ACTION),
                    Mockito.anyString(),
                    Mockito.any()))
                    .thenReturn(getAddActionTemplate());
            when(cassandraOperation.getRecordsByProperty(
                    Mockito.eq(JsonKey.SUNBIRD_NOTIFICATIONS),
                    Mockito.eq("notification_template"),
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.any()))
                    .thenReturn(getNotificationTemplate());
            when(cassandraOperation.batchInsert(
                    Mockito.anyString(), Mockito.anyString(), Mockito.anyList(),Mockito.any()))
                    .thenReturn(getCassandraResponse());
            when(cassandraOperation.getRecordById(
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.anyMap(),
                    Mockito.anyMap()))
                    .thenReturn(getNotificationFeedResponse());

            when(cassandraOperation.getRecordsByProperty(
                    Mockito.eq(JsonKey.SUNBIRD_NOTIFICATIONS),
                    Mockito.eq("feed_version_map"),
                    Mockito.anyString(),
                    Mockito.anyList(),
                    Mockito.any()))
                    .thenReturn(getFeedMapList());
            when(cassandraOperation.batchDelete(
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.anyList(),
                    Mockito.anyMap()))
                    .thenReturn(getCassandraResponse());

        }catch (BaseException be) {
            Assert.assertTrue(false);
        }

        Request request = getV2NotificationTypeMissingRequest();
        subject.tell(request, probe.getRef());
        BaseException ex = probe.expectMsgClass(Duration.ofSeconds(80), BaseException.class);
        Assert.assertTrue(ex.getResponseCode()==400);

    }
    private Request getV2NotificationRequestWithParamMissing() {
        Request reqObj = new Request();
        Map<String, Object> context = new HashMap<>();
        context.put(JsonKey.USER_ID, "user1");
        reqObj.setContext(context);
        reqObj.setOperation("createNotification");
        Map<String, Object> reqMap = new HashMap<>();
        Map<String,Object> notification = new HashMap<>();
        Map<String,Object> action = new HashMap<>();
        Map<String,Object> template = new HashMap<>();
        action.put(JsonKey.TEMPLATE,template);
        Map<String,Object> createdBy = new HashMap<>();
        createdBy.put(JsonKey.ID,"12354");
        createdBy.put(JsonKey.TYPE,JsonKey.USER);
        action.put(JsonKey.CREATED_BY,createdBy);
        action.put(JsonKey.ADDITIONAL_INFO,new HashMap<>());
        action.put(JsonKey.TYPE,"add-member");
        action.put(JsonKey.CATEGORY,"certificates");
        notification.put(JsonKey.ACTION,action);
        notification.put(JsonKey.IDS, Arrays.asList("1234"));
        notification.put(JsonKey.TYPE,"feed");
        notification.put("priority",1);
        reqMap.put(JsonKey.NOTIFICATIONS,Arrays.asList(notification));
        reqObj.setRequest(reqMap);
        return reqObj;
    }

    private Request getV1NotificationRequest() {
        Request reqObj = new Request();
        Map<String, Object> context = new HashMap<>();
        context.put(JsonKey.USER_ID, "user1");
        reqObj.setContext(context);
        reqObj.setOperation("createNotification");
        Map<String, Object> reqMap = new HashMap<>();
        Map<String,Object> notification = new HashMap<>();
        Map<String,Object> data = new HashMap<>();
        Map<String,Object> actionData = new HashMap<>();
        actionData.put(JsonKey.IDENTIFIER,"1233443");
        actionData.put(JsonKey.ACTION_TYPE,"add-member");
        actionData.put(JsonKey.CATEGORY,"certificates");
        data.put(JsonKey.ACTION_DATA,actionData);
        notification.put(JsonKey.USER_ID,"1234");
        notification.put(JsonKey.DATA,data);
        notification.put("priority",1);
        reqMap.put(JsonKey.NOTIFICATIONS,Arrays.asList(notification));
        reqMap.put("version","v1");
        reqObj.setRequest(reqMap);
        return reqObj;
    }



    private Response getNotificationTemplate() {
        List<Map<String,Object>> templateIdDetails = new ArrayList<>();
        Map<String,Object> templateObj = new HashMap<>();
        templateObj.put("ver","4.2.0");
        templateObj.put("type","JSON");
        templateObj.put("templateId","something_is_added_to");
        templateObj.put("data","{\"title\":\"${param1}has been added\"}");
        templateObj.put("templateId","something_is_added_to");
        templateObj.put("template_schema","{\"$schema\":\"#/definition/params\",\"title\":\"params context\",\"description\":\"properties Data\",\"type\":\"object\",\"properties\":{\"param1\":{\"description\":\"property 1 value\",\"type\":\"string\"}},\"required\":[\"param1\"]}");
        templateIdDetails.add(templateObj);
        Map<String, Object> result = new HashMap<>();
        result.put(JsonKey.RESPONSE, templateIdDetails);
        Response response = new Response();
        response.putAll(result);
        return response;
    }




    private Response getAddActionTemplate() {
        List<Map<String,Object>> templateIdDetails = new ArrayList<>();
        Map<String,Object> templateObj = new HashMap<>();
        templateObj.put("action","add-member");
        templateObj.put("type","FEED");
        templateObj.put("templateId","something_is_added_to");
        templateIdDetails.add(templateObj);
        Map<String, Object> result = new HashMap<>();
        result.put(JsonKey.RESPONSE, templateIdDetails);
        Response response = new Response();
        response.putAll(result);
        return response;
    }


    private Request getV2NotificationRequest() {
        Request reqObj = new Request();
        Map<String, Object> context = new HashMap<>();
        context.put(JsonKey.USER_ID, "user1");
        reqObj.setContext(context);
        reqObj.setOperation("createNotification");
        Map<String, Object> reqMap = new HashMap<>();
        Map<String,Object> notification = new HashMap<>();
        Map<String,Object> action = new HashMap<>();
        Map<String,Object> template = new HashMap<>();
        Map<String,Object> params = new HashMap<>();
        params.put("param1","group");
        template.put(JsonKey.PARAMS,params);
        action.put(JsonKey.TEMPLATE,template);
        Map<String,Object> createdBy = new HashMap<>();
        createdBy.put(JsonKey.ID,"12354");
        createdBy.put(JsonKey.TYPE,JsonKey.USER);
        action.put(JsonKey.CREATED_BY,createdBy);
        action.put(JsonKey.ADDITIONAL_INFO,new HashMap<>());
        action.put(JsonKey.TYPE,"add-member");
        action.put(JsonKey.CATEGORY,"certificates");
        notification.put(JsonKey.ACTION,action);
        notification.put(JsonKey.IDS, Arrays.asList("1234"));
        notification.put(JsonKey.TYPE,"feed");
        notification.put("priority",1);
        reqMap.put(JsonKey.NOTIFICATIONS,Arrays.asList(notification));
        reqObj.setRequest(reqMap);
        return reqObj;
    }

    private Request getV2NotificationTypeMissingRequest() {
        Request reqObj = new Request();
        Map<String, Object> context = new HashMap<>();
        context.put(JsonKey.USER_ID, "user1");
        reqObj.setContext(context);
        reqObj.setOperation("createNotification");
        Map<String, Object> reqMap = new HashMap<>();
        Map<String,Object> notification = new HashMap<>();
        Map<String,Object> action = new HashMap<>();
        Map<String,Object> template = new HashMap<>();
        Map<String,Object> params = new HashMap<>();
        params.put("param1","group");
        template.put(JsonKey.PARAMS,params);
        template.put(JsonKey.DATA,"{\"title\":\"This is title\"}");
        action.put(JsonKey.TEMPLATE,template);
        Map<String,Object> createdBy = new HashMap<>();
        createdBy.put(JsonKey.ID,"12354");
        createdBy.put(JsonKey.TYPE,JsonKey.USER);
        action.put(JsonKey.CREATED_BY,createdBy);
        action.put(JsonKey.ADDITIONAL_INFO,new HashMap<>());
        action.put(JsonKey.TYPE,"add-member");
        action.put(JsonKey.CATEGORY,"certificates");
        notification.put(JsonKey.ACTION,action);
        notification.put(JsonKey.IDS, Arrays.asList("1234"));
        notification.put(JsonKey.TYPE,"feed");
        notification.put("priority",1);
        reqMap.put(JsonKey.NOTIFICATIONS,Arrays.asList(notification));
        reqObj.setRequest(reqMap);
        return reqObj;
    }
}
