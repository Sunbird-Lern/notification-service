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
import java.util.Arrays;
import java.util.HashMap;
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

public class DeleteNotificationActorTest extends BaseActorTest{

    public  final Props props = Props.create(DeleteNotificationActor.class);

    @Before
    public void setUp() throws Exception {

        PowerMockito.mockStatic(Localizer.class);
        Mockito.when(Localizer.getInstance()).thenReturn(null);
        PowerMockito.mockStatic(SystemConfigUtil.class);

    }

    @Test
    public void testDeleteSuccess(){
        Request request = new Request();
        Map<String,Object> reqObj = new HashMap<>();
        reqObj.put(JsonKey.IDS, Arrays.asList("123213213"));
        reqObj.put(JsonKey.USER_ID,"123456");
        reqObj.put(JsonKey.CATEGORY,"groups");
        request.setOperation(JsonKey.DELETE_FEED);
        request.setRequest(reqObj);
        Map<String,Object> reqContext =  new HashMap<>();
        reqContext.put(JsonKey.USER_ID,"123456");
        request.setContext(reqContext);
        TestKit probe = new TestKit(system);
        ActorRef subject = system.actorOf(props);
        CassandraOperation cassandraOperation;
        PowerMockito.mockStatic(ServiceFactory.class);
        cassandraOperation = mock(CassandraOperationImpl.class);
        when(ServiceFactory.getInstance()).thenReturn(cassandraOperation);
        when(cassandraOperation.batchDelete(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyList(),
                Mockito.anyMap()))
                .thenReturn(getCassandraResponse());

        subject.tell(request, probe.getRef());
        Response res = probe.expectMsgClass(Duration.ofSeconds(80), Response.class);
        System.out.println(res.getResult());
        Assert.assertTrue(null != res && res.getResponseCode().getCode()==200);
    }

    @Test
    public void testDeleteAuthorizationException(){
        Request request = new Request();
        Map<String,Object> reqObj = new HashMap<>();
        reqObj.put(JsonKey.IDS, Arrays.asList("123213213"));
        reqObj.put(JsonKey.USER_ID,"123456");
        reqObj.put(JsonKey.CATEGORY,"groups");
        request.setOperation(JsonKey.DELETE_FEED);
        request.setRequest(reqObj);
        Map<String,Object> reqContext =  new HashMap<>();
        reqContext.put(JsonKey.USER_ID,"1234567");
        request.setContext(reqContext);
        TestKit probe = new TestKit(system);
        ActorRef subject = system.actorOf(props);
        CassandraOperation cassandraOperation;
        PowerMockito.mockStatic(ServiceFactory.class);
        cassandraOperation = mock(CassandraOperationImpl.class);
        when(ServiceFactory.getInstance()).thenReturn(cassandraOperation);
        when(cassandraOperation.batchDelete(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyList(),
                Mockito.anyMap()))
                .thenReturn(getCassandraResponse());

        subject.tell(request, probe.getRef());

        BaseException ex = probe.expectMsgClass(Duration.ofSeconds(80), BaseException.class);
        Assert.assertTrue(null != ex && ex.getResponseCode()==401);
    }

    @Test
    public void testV1DeleteSuccess(){
        Request request = new Request();
        Map<String,Object> reqObj = new HashMap<>();
        reqObj.put(JsonKey.IDS, Arrays.asList("123213213"));
        reqObj.put(JsonKey.USER_ID,"123456");
        reqObj.put(JsonKey.CATEGORY,"groups");
        request.setOperation(JsonKey.DELETE_V1_FEED);
        request.setRequest(reqObj);
        Map<String,Object> reqContext =  new HashMap<>();
        reqContext.put(JsonKey.USER_ID,"123456");
        request.setContext(reqContext);
        TestKit probe = new TestKit(system);
        ActorRef subject = system.actorOf(props);
        CassandraOperation cassandraOperation;
        PowerMockito.mockStatic(ServiceFactory.class);
        cassandraOperation = mock(CassandraOperationImpl.class);
        when(ServiceFactory.getInstance()).thenReturn(cassandraOperation);
        when(cassandraOperation.batchDelete(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyList(),
                Mockito.anyMap()))
                .thenReturn(getCassandraResponse());

        subject.tell(request, probe.getRef());
        Response res = probe.expectMsgClass(Duration.ofSeconds(80), Response.class);
        System.out.println(res.getResult());
        Assert.assertTrue(null != res && res.getResponseCode().getCode()==200);
    }
}
