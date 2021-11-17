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

@PowerMockIgnore({"javax.management.*", "jdk.internal.reflect.*"})
public class UpdateNotificationActorTest extends BaseActorTest{
    public  final Props props = Props.create(UpdateNotificationActor.class);

    public  PropertiesCache propertiesCache;
    @Before
    public void setUp() throws Exception {

        PowerMockito.mockStatic(Localizer.class);
        Mockito.when(Localizer.getInstance()).thenReturn(null);
        PowerMockito.mockStatic(SystemConfigUtil.class);
        PowerMockito.mockStatic(PropertiesCache.class);
        propertiesCache = Mockito.mock(PropertiesCache.class);
        Mockito.when(PropertiesCache.getInstance()).thenReturn(propertiesCache);
        when(propertiesCache.getProperty(org.sunbird.JsonKey.VERSION_SUPPORT_CONFIG_ENABLE)).thenReturn("true");

    }

    @Test
    public void testUpdateStatusSuccess(){
        Request request = new Request();
        Map<String,Object> reqObj = new HashMap<>();
        reqObj.put(JsonKey.IDS, Arrays.asList("123213213"));
        reqObj.put(JsonKey.USER_ID,"123456");
        reqObj.put(JsonKey.STATUS,"read");
        request.setOperation(JsonKey.UPDATE_FEED);
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
        when(cassandraOperation.batchUpdate(
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
    public void testV1UpdateStatusSuccess(){
        Request request = new Request();
        Map<String,Object> reqObj = new HashMap<>();
        reqObj.put(JsonKey.IDS, Arrays.asList("123213213"));
        reqObj.put(JsonKey.USER_ID,"123456");
        reqObj.put(JsonKey.STATUS,"read");
        request.setOperation(JsonKey.UPDATE_V1_FEED);
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
        when(cassandraOperation.batchUpdate(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyList(),
                Mockito.anyMap()))
                .thenReturn(getCassandraResponse());
        when(cassandraOperation.getRecordsByPrimaryKeys(
                Mockito.anyString(),
                Mockito.eq("feed_version_map"),
                Mockito.anyList(),
                Mockito.anyString(),
                Mockito.anyMap()))
                .thenReturn(getFeedMapList());
        subject.tell(request, probe.getRef());
        Response res = probe.expectMsgClass(Duration.ofSeconds(80), Response.class);
        System.out.println(res.getResult());
        Assert.assertTrue(null != res && res.getResponseCode().getCode()==200);
    }
}
