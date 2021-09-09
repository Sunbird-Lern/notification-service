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
import org.sunbird.JsonKey;
import org.sunbird.cassandra.CassandraOperation;
import org.sunbird.cassandraimpl.CassandraOperationImpl;
import org.sunbird.common.message.Localizer;
import org.sunbird.common.request.Request;
import org.sunbird.common.response.Response;
import org.sunbird.notification.email.Email;
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
public class ReadNotificationFeedActorTest extends BaseActorTest{

    public  final Props props = Props.create(ReadNotificationFeedActor.class);

    public  PropertiesCache propertiesCache;
    @Before
    public void setUp() throws Exception {

        PowerMockito.mockStatic(Localizer.class);
        Mockito.when(Localizer.getInstance()).thenReturn(null);
        PowerMockito.mockStatic(SystemConfigUtil.class);
        PowerMockito.mockStatic(PropertiesCache.class);
        propertiesCache = Mockito.mock(PropertiesCache.class);
        Mockito.when(PropertiesCache.getInstance()).thenReturn(propertiesCache);
    }


    @Test
    public void readV1FeedTestSuccess(){
        Request request = getV1Request();
        TestKit probe = new TestKit(system);
        ActorRef subject = system.actorOf(props);
        CassandraOperation cassandraOperation;
        PowerMockito.mockStatic(ServiceFactory.class);
        cassandraOperation = mock(CassandraOperationImpl.class);
        when(ServiceFactory.getInstance()).thenReturn(cassandraOperation);
        when(cassandraOperation.getRecordById(
               Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyMap(),
                Mockito.anyMap()))
                .thenReturn(getNotificationFeedResponse());

        subject.tell(request, probe.getRef());
        Response res = probe.expectMsgClass(Duration.ofSeconds(80), Response.class);
        System.out.println(res.getResult());
        Assert.assertTrue(null != res && res.getResponseCode().getCode()==200);
    }

    @Test
    public void readV2FeedTestSuccess(){
        Request request = getV2Request();
        TestKit probe = new TestKit(system);
        ActorRef subject = system.actorOf(props);
        CassandraOperation cassandraOperation;
        PowerMockito.mockStatic(ServiceFactory.class);
        cassandraOperation = mock(CassandraOperationImpl.class);
        when(ServiceFactory.getInstance()).thenReturn(cassandraOperation);
        when(cassandraOperation.getRecordById(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyMap(),
                Mockito.anyMap()))
                .thenReturn(getNotificationFeedResponse());

        subject.tell(request, probe.getRef());
        Response res = probe.expectMsgClass(Duration.ofSeconds(80), Response.class);
        System.out.println(res.getResult());
        Assert.assertTrue(null != res && res.getResponseCode().getCode()==200);
    }

    private Response getNotificationFeedResponse() {
        Response response = new Response();
        Map<String, Object> result = new HashMap<>();
        List<Map<String,Object>> notificationFeeds = new ArrayList<>();

        Map<String,Object> notificationV1Feed = new HashMap<>();
        notificationV1Feed.put(JsonKey.USER_ID,"123456");
        notificationV1Feed.put(JsonKey.ID,"13213213123131");
        notificationV1Feed.put(JsonKey.PRIORITY,1);
        notificationV1Feed.put(JsonKey.STATUS,"unread");
        notificationV1Feed.put(JsonKey.CATEGORY,"Groups");
        notificationV1Feed.put(JsonKey.ACTION,"{\"actionData\":{\"title\":\"test is game\",\"description\":\"This is desc\",\"contentUrl\":\"http://www.sunbird.org/test\"}}");
        notificationV1Feed.put(JsonKey.VERSION,"v1");

        Map<String,Object> notificationV2Feed = new HashMap<>();
        notificationV1Feed.put(JsonKey.USER_ID,"123456");
        notificationV1Feed.put(JsonKey.ID,"13213213123131");
        notificationV1Feed.put(JsonKey.PRIORITY,1);
        notificationV1Feed.put(JsonKey.STATUS,"unread");
        notificationV1Feed.put(JsonKey.CATEGORY,"Groups");
        notificationV1Feed.put(JsonKey.ACTION,"{\"type\":\"add-member\",\"category\":\"groups\",\"template\":{\"data\":\"{\\\"title\\\":\\\"youhavebeenadded\\\"}\",\"type\":\"JSON\"},\"createdBy\":{\"id\":\"12321323\"},\"additionalInfo\":{\"identifier\":\"1323213\"}}");
        notificationFeeds.add(notificationV1Feed);
        notificationFeeds.add(notificationV2Feed);
        result.put(JsonKey.RESPONSE,notificationFeeds);
        response.putAll(result);
        return response;
    }

    private Request getV1Request() {
        Request request = new Request();
        Map<String,Object> reqObj = new HashMap<>();
        reqObj.put(JsonKey.USER_ID,"123456");
        reqObj.put(JsonKey.VERSION,"v1");
        request.setOperation("readV1Feed");
        request.setRequest(reqObj);
        return request;
    }

    private Request getV2Request() {
        Request request = new Request();
        Map<String,Object> reqObj = new HashMap<>();
        reqObj.put(JsonKey.USER_ID,"123456");
        request.setOperation("readFeed");
        Map<String,Object> reqContext =  new HashMap<>();
        reqContext.put(JsonKey.USER_ID,"123456");
        request.setContext(reqContext);
        request.setRequest(reqObj);
        return request;
    }

}
