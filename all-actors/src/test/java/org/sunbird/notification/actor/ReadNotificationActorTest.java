package org.sunbird.notification.actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
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
import org.sunbird.common.exception.BaseException;
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
public class ReadNotificationActorTest extends BaseActorTest{

    public  final Props props = Props.create(ReadNotificationActor.class);

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

    @Test
    public void readV1FeedTestFailed(){
        Request request = getV1MissingUserRequest();
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
        BaseException ex = probe.expectMsgClass(Duration.ofSeconds(80), BaseException.class);
        Assert.assertEquals("Mandatory parameter userId is missing",ex.getMessage());

    }


    private Request getV1Request() {
        Request request = new Request();
        Map<String,Object> reqObj = new HashMap<>();
        reqObj.put(JsonKey.USER_ID,"12334");
        reqObj.put(JsonKey.VERSION,"v1");
        request.setOperation("readV1Feed");
        request.setRequest(reqObj);
        return request;
    }
    private Request getV1MissingUserRequest() {
        Request request = new Request();
        Map<String,Object> reqObj = new HashMap<>();
        reqObj.put(JsonKey.USER_ID,"");
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
