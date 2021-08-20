package org.sunbird.notification.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.datastax.driver.core.ResultSet;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.typesafe.config.Config;
import org.apache.commons.math3.analysis.function.Pow;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicStatusLine;
import org.apache.kafka.clients.producer.Producer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
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

import java.io.IOException;
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
    public  Email emailService ;
    public  CloseableHttpClient httpClients;
    @Before
    public void setUp() throws Exception {

        PowerMockito.mockStatic(Localizer.class);
        Mockito.when(Localizer.getInstance()).thenReturn(null);
        PowerMockito.mockStatic(SystemConfigUtil.class);
        PowerMockito.mockStatic(PropertiesCache.class);
        propertiesCache = Mockito.mock(PropertiesCache.class);
        Mockito.when(PropertiesCache.getInstance()).thenReturn(propertiesCache);
        PowerMockito.mockStatic(Email.class);
        emailService= Mockito.mock(Email.class);
        Mockito.when(Email.getInstance(Mockito.any())).thenReturn(emailService);
        PowerMockito.mockStatic(HttpClients.class);
        httpClients = Mockito.mock(CloseableHttpClient.class);
        Mockito.when(HttpClients.createDefault()).thenReturn(httpClients);

    }

    @Test
    public void testCreateFeedNotificationSuccess(){

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
                    Mockito.anyString(),
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
        }catch (BaseException be) {
            Assert.assertTrue(false);
        }

        Request request = getV2NotificationRequest();
        subject.tell(request, probe.getRef());
        Response res = probe.expectMsgClass(Duration.ofSeconds(40), Response.class);
        System.out.println(res.getResult());
        Assert.assertTrue(null != res && res.getResponseCode().getCode()==200);

    }

    @Test
    public void testCreateEmailSyncNotificationSuccess(){

        TestKit probe = new TestKit(system);
        ActorRef subject = system.actorOf(props);
        try {
            CassandraOperation cassandraOperation ;
            PowerMockito.mockStatic(ServiceFactory.class);
            cassandraOperation = mock(CassandraOperationImpl.class);
            when(ServiceFactory.getInstance()).thenReturn(cassandraOperation);
            Mockito.when(emailService.sendMail(Mockito.anyList(),Mockito.anyString(),Mockito.anyString())).thenReturn(true);

            when(cassandraOperation.getRecordsByProperty(
                    Mockito.eq(JsonKey.SUNBIRD_NOTIFICATIONS),
                    Mockito.eq("action_template"),
                    Mockito.anyString(),
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

        }catch (BaseException be) {
            Assert.assertTrue(false);
        }

        Request request = getV2NotificationEmailRequest();
        subject.tell(request, probe.getRef());
        Response res = probe.expectMsgClass(Duration.ofSeconds(40), Response.class);
        System.out.println(res.getResult());
        Assert.assertTrue(null != res && res.getResponseCode().getCode()==200);
    }

    @Test
    public void testCreatePhoneSyncNotificationSuccess(){

        TestKit probe = new TestKit(system);
        ActorRef subject = system.actorOf(props);
        try {
            Mockito.when(propertiesCache.getProperty(Mockito.anyString())).thenReturn("randomString");
            CassandraOperation cassandraOperation ;
            PowerMockito.mockStatic(ServiceFactory.class);
            cassandraOperation = mock(CassandraOperationImpl.class);
            when(ServiceFactory.getInstance()).thenReturn(cassandraOperation);
            Mockito.when(emailService.sendMail(Mockito.anyList(),Mockito.anyString(),Mockito.anyString())).thenReturn(true);
            CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
            Mockito.when(closeableHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("http",1,1),200,null));
            Mockito.doNothing().when(closeableHttpResponse).close();
            when(cassandraOperation.getRecordsByProperty(
                    Mockito.eq(JsonKey.SUNBIRD_NOTIFICATIONS),
                    Mockito.eq("action_template"),
                    Mockito.anyString(),
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

            Mockito.when(httpClients.execute(Mockito.any())).thenReturn(closeableHttpResponse);

        }catch (BaseException be) {
            Assert.assertTrue(false);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Request request = getV2NotificationPhoneRequest();
        subject.tell(request, probe.getRef());
        Response res = probe.expectMsgClass(Duration.ofSeconds(40), Response.class);
        System.out.println(res.getResult());
        Assert.assertTrue(null != res && res.getResponseCode().getCode()==200);
    }



    private Request getV2NotificationEmailRequest() {
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
        Map<String,Object> additionalInfo = new HashMap<>();
        additionalInfo.put("sender","sender@subird.com");
        additionalInfo.put("subject","Hello User");
        action.put(JsonKey.ADDITIONAL_INFO,additionalInfo);
        action.put(JsonKey.TYPE,"add-member");
        action.put(JsonKey.CATEGORY,"groups");
        notification.put(JsonKey.ACTION,action);
        notification.put(JsonKey.IDS, Arrays.asList("1234"));
        notification.put(JsonKey.TYPE,"email");
        notification.put("priority",1);
        reqMap.put(JsonKey.NOTIFICATIONS,Arrays.asList(notification));
        reqObj.setManagerName("sync");
        reqObj.setRequest(reqMap);
        return reqObj;
    }

    private Request getV2NotificationPhoneRequest() {
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
        createdBy.put(JsonKey.TYPE,JsonKey.SYSTEM);
        action.put(JsonKey.CREATED_BY,createdBy);
        Map<String,Object> additionalInfo = new HashMap<>();
        additionalInfo.put("sender","sender@subird.com");
        additionalInfo.put("subject","Hello User");
        action.put(JsonKey.ADDITIONAL_INFO,additionalInfo);
        action.put(JsonKey.TYPE,"sms");
        action.put(JsonKey.CATEGORY,"user-service");
        notification.put(JsonKey.ACTION,action);
        notification.put(JsonKey.IDS, Arrays.asList("98128121212"));
        notification.put(JsonKey.TYPE,"phone");
        notification.put("priority",1);
        reqMap.put(JsonKey.NOTIFICATIONS,Arrays.asList(notification));
        reqObj.setManagerName("sync");
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
        action.put(JsonKey.CATEGORY,"groups");
        notification.put(JsonKey.ACTION,action);
        notification.put(JsonKey.IDS, Arrays.asList("1234"));
        notification.put(JsonKey.TYPE,"feed");
        notification.put("priority",1);
        reqMap.put(JsonKey.NOTIFICATIONS,Arrays.asList(notification));
        reqObj.setRequest(reqMap);
        return reqObj;
    }
}
