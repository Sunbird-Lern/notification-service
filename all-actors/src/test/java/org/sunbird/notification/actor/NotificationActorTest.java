package org.sunbird.notification.actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.sun.mail.util.PropUtil;
import com.typesafe.config.ConfigFactory;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.sunbird.common.message.Localizer;
import org.sunbird.common.response.Response;
import org.sunbird.notification.beans.Constants;
import org.sunbird.notification.beans.MessageResponse;
import org.sunbird.notification.dispatcher.NotificationRouter;
import org.sunbird.notification.fcm.providerImpl.FCMHttpNotificationServiceImpl;
import org.sunbird.notification.utils.NotificationConstant;
import org.sunbird.notification.utils.Util;
import org.sunbird.pojo.Config;
import org.sunbird.pojo.NotificationRequest;
import org.sunbird.pojo.OTP;
import org.sunbird.pojo.Template;
import org.sunbird.util.ConfigUtil;
import org.sunbird.util.Constant;
import org.sunbird.util.kafka.KafkaClient;
import org.sunbird.utils.PropertiesCache;
import org.sunbird.common.request.Request;
import org.sunbird.util.SystemConfigUtil;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.sunbird.JsonKey;
@RunWith(PowerMockRunner.class)
@PrepareForTest({
        PropertiesCache.class,
        Util.class,
        Session.class,
        PropUtil.class,
        Localizer.class,
        SystemConfigUtil.class,
        HttpClients.class,
        StatusLine.class,
        Unirest.class,
        ConfigUtil.class,
        System.class,
        KafkaClient.class,
        Producer.class

})
@PowerMockIgnore({"javax.management.*", "jdk.internal.reflect.*"})
public class NotificationActorTest extends BaseActorTest{

    public  final Props props = Props.create(NotificationActor.class);
    private ObjectMapper mapper = new ObjectMapper();
    String BOOTSTRAP_SERVERS="http://bootstrap_server.com";
    String topic="kafka";

    @Before
    public void setUp() throws MessagingException, IOException, UnirestException {
        PowerMockito.mockStatic(Localizer.class);
        Mockito.when(Localizer.getInstance()).thenReturn(null);
        PowerMockito.mockStatic(SystemConfigUtil.class);

        PowerMockito.mockStatic(Util.class);
        PowerMockito.when(Util.readValue(Mockito.eq(Constants.EMAIL_SERVER_HOST))).thenReturn("http://localhost:9191");
        PowerMockito.when(Util.readValue(Mockito.eq(Constants.EMAIL_SERVER_PORT))).thenReturn("1234");
        PowerMockito.when(Util.readValue(Mockito.eq(Constants.EMAIL_SERVER_USERNAME))).thenReturn("john12");
        PowerMockito.when(Util.readValue(Mockito.eq(Constants.EMAIL_SERVER_PASSWORD))).thenReturn("123#4343");
        PowerMockito.when(Util.readValue(Mockito.eq(Constants.EMAIL_SERVER_FROM))).thenReturn("info@sunbird.com");
        Session session = mockSession();
        PowerMockito.mockStatic(PropUtil.class);
        PowerMockito.when(PropUtil.getBooleanSessionProperty(Mockito.eq(session),Mockito.eq("mail.mime.address.strict")  ,Mockito.eq(true))).thenReturn(true);
        PowerMockito.when(Util.readValue(Mockito.eq(NotificationConstant.SUNBIRD_MSG_91_BASEURL))).thenReturn("http://sms");
        PowerMockito.when(Util.readValue(Mockito.eq(NotificationConstant.SUNBIRD_MSG_91_POST_URL))).thenReturn("http://sms");
        PowerMockito.when(Util.readValue(Mockito.eq(NotificationConstant.SUNBIRD_MSG_91_ROUTE))).thenReturn("http://sms");
        PowerMockito.when(Util.readValue(Mockito.eq(NotificationConstant.SUNBIRD_DEFAULT_COUNTRY_CODE))).thenReturn("91");
        PowerMockito.when(Util.readValue(Mockito.eq(NotificationConstant.SUNBIRD_OTP_DEFAULT_LENGHT))).thenReturn("4");
        PowerMockito.when(Util.readValue(Mockito.eq(NotificationConstant.SUNBIRD_OTP_DEFAULT_MESSAGE))).thenReturn("4");
        PowerMockito.when(Util.readValue(Mockito.eq(NotificationConstant.SUNBIRD_OTP_DEFAULT_EXPIRY_IN_MINUTE))).thenReturn("4");

        PowerMockito.mockStatic(HttpClients.class);
        CloseableHttpClient client = Mockito.mock(CloseableHttpClient.class);
        CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);
        PowerMockito.when(HttpClients.createDefault()).thenReturn(client);
        PowerMockito.when(client.execute(Mockito.any())).thenReturn(response);
        PowerMockito.doNothing().when(client).close();

        StatusLine sl = Mockito.mock(StatusLine.class);
        PowerMockito.when(sl.getStatusCode()).thenReturn(200);
        PowerMockito.when(response.getStatusLine()).thenReturn(sl);

        PowerMockito.mockStatic(Unirest.class);
        GetRequest getRequest = Mockito.mock(GetRequest.class);
        com.mashape.unirest.http.HttpResponse response1  = Mockito.mock(com.mashape.unirest.http.HttpResponse.class);
        Mockito.when(response1.getStatus()).thenReturn(NotificationConstant.SUCCESS_CODE);
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setType(NotificationConstant.SUCCESS);
        messageResponse.setMessage("OTP verification");
        messageResponse.setCode("1234");
        Mockito.when(response1.getBody()).thenReturn(mapper.writeValueAsString(messageResponse));
        Mockito.when(getRequest.header(Mockito.anyString(),Mockito.anyString())).thenReturn(getRequest);
        Mockito.when(getRequest.asString()).thenReturn((com.mashape.unirest.http.HttpResponse) response1);
        PowerMockito.when(Unirest.get(Mockito.anyString())).thenReturn(getRequest);
        PowerMockito.mockStatic(ConfigUtil.class);
        com.typesafe.config.Config defaultConfig = Mockito.mock(com.typesafe.config.Config.class);
        PowerMockito.when(ConfigUtil.getConfig()).thenReturn(defaultConfig);
        Mockito.when(defaultConfig.getString(Constant.SUNBIRD_NOTIFICATION_KAFKA_SERVICE_CONFIG)).thenReturn(BOOTSTRAP_SERVERS);
        Mockito.when(defaultConfig.getString(Constant.SUNBIRD_NOTIFICATION_KAFKA_TOPIC)).thenReturn(topic);
        PowerMockito.mockStatic(KafkaClient.class);
        Producer producer = Mockito.mock(KafkaProducer.class);
        PowerMockito.when(KafkaClient.createProducer(Mockito.anyString(),Mockito.anyString())).thenReturn(producer);
    }


    @Test
    public void syncEmailNotificationSuccess() throws IOException {
        TestKit probe = new TestKit(system);
        ActorRef subject = system.actorOf(props);
        Request request = getNotificationRequest();
        subject.tell(request, probe.getRef());
        Response res = probe.expectMsgClass(Duration.ofSeconds(80), Response.class);
        System.out.println(res.getResult());
        Assert.assertTrue(null != res && res.getResponseCode().getCode()==200);
    }

    @Test
    public void verifyOtpNotificationSuccess() throws IOException {
        TestKit probe = new TestKit(system);
        ActorRef subject = system.actorOf(props);
        Request request = getVerifyOtpNotificationRequest();
        subject.tell(request, probe.getRef());
        Response res = probe.expectMsgClass(Duration.ofSeconds(80), Response.class);
        System.out.println(res.getResult());
        Assert.assertTrue(null != res && res.getResponseCode().getCode()==200);
    }

    @Test
    public void otpNotificationSuccess() throws IOException {
        TestKit probe = new TestKit(system);
        ActorRef subject = system.actorOf(props);
        Request request = getOtpNotificationRequest();
        subject.tell(request, probe.getRef());
        Response res = probe.expectMsgClass(Duration.ofSeconds(80), Response.class);
        System.out.println(res.getResult());
        Assert.assertTrue(null != res && res.getResponseCode().getCode()==200);
    }



    @Test
    public void fcmNotificationSuccess() throws IOException {
        TestKit probe = new TestKit(system);
        ActorRef subject = system.actorOf(props);
        Request request = getFCMNotificationRequest();
        subject.tell(request, probe.getRef());
        Response res = probe.expectMsgClass(Duration.ofSeconds(1000), Response.class);
        System.out.println(res.getResult());
        Assert.assertTrue(null != res && res.getResponseCode().getCode()==200);
    }
    private Request getNotificationRequest() throws IOException {
          Request req = new Request() ;
          NotificationRequest request = new NotificationRequest();
          request.setIds(Arrays.asList("123454321"));
          request.setMode("email");
          request.setDeliveryType(NotificationRouter.DeliveryType.message.name());
          Template template = new Template();
          template.setData("hello ${param1}");
          String jsonString= "{\"param1\":\"sunbird\"}";
          template.setParams(mapper.readTree(jsonString));
          request.setTemplate(template);
          Config emailConfig = new Config();
          emailConfig.setSubject("Welcome");
          request.setConfig(emailConfig);
          req.setManagerName("sync");
          Map<String,Object> reqMap = new HashMap<>();
          reqMap.put(JsonKey.NOTIFICATIONS,Arrays.asList(mapper.convertValue(request,Map.class)));
          req.setRequest(reqMap);
          req.setOperation(JsonKey.NOTIFICATION);
          return req    ;
    }

    private Request getOtpNotificationRequest() throws IOException {
        Request req = new Request() ;
        NotificationRequest request = new NotificationRequest();
        request.setIds(Arrays.asList("123454321"));
        request.setMode("phone");
        request.setDeliveryType(NotificationRouter.DeliveryType.otp.name());
        Template template = new Template();
        template.setData("hello ${param1}");
        String jsonString= "{\"param1\":\"sunbird\"}";
        template.setParams(mapper.readTree(jsonString));
        request.setTemplate(template);
        Config phoneConfig = new Config();
        OTP otp = new OTP();
        otp.setLength(20);
        otp.setExpiryInMinute(10);
        phoneConfig.setOtp(otp);
        phoneConfig.setSubject("Welcome");
        request.setConfig(phoneConfig);
        req.setManagerName("sync");
        Map<String,Object> reqMap = new HashMap<>();
        reqMap.put(JsonKey.NOTIFICATIONS,Arrays.asList(mapper.convertValue(request,Map.class)));
        req.setRequest(reqMap);
        req.setOperation(JsonKey.NOTIFICATION);
        return req    ;
    }


    private Request getFCMNotificationRequest() throws IOException {
        Request req = new Request() ;

        NotificationRequest request = new NotificationRequest();
        Map<String,Object> rawData = new HashMap<>();
        rawData.put("identifier","1213121");
        rawData.put("contentUrl","www://htttp:image/123434");
        request.setDeliveryType(NotificationRouter.DeliveryType.message.name());
        request.setRawData(mapper.valueToTree(rawData));
        request.setMode("device");
        Config deviceConfig = new Config();
        deviceConfig.setTopic("kafka");
        request.setConfig(deviceConfig);
        req.setManagerName("sync");
        Map<String,Object> reqMap = new HashMap<>();
        reqMap.put(JsonKey.NOTIFICATIONS,Arrays.asList(mapper.convertValue(request,Map.class)));
        req.setRequest(reqMap);
        req.setOperation(JsonKey.NOTIFICATION);
        return req    ;
    }
    private Request getVerifyOtpNotificationRequest() throws IOException {
        Request req = new Request() ;
        Map<String,Object> request = new HashMap<>();
        request.put(NotificationConstant.KEY,"7911111111");
        request.put(NotificationConstant.VALUE,"1323");
        req.setManagerName("sync");
        req.setRequest(request);
        req.setOperation(JsonKey.VERIFY_OTP);
        return req ;
    }
    private Session mockSession() throws MessagingException {
        PowerMockito.mockStatic(Session.class);
        Session session = Mockito.mock(Session.class);
        PowerMockito.when(Session.getInstance(Mockito.any(),Mockito.any())).thenReturn(session);
        Transport transport = Mockito.mock(Transport.class);
        Mockito.when(session.getTransport(Mockito.anyString())).thenReturn(transport);
        Mockito.doNothing().when(transport).connect(Mockito.anyString(),Mockito.anyString(),Mockito.anyString());
        Mockito.doNothing().when(transport).sendMessage(Mockito.any(),Mockito.any());
        Mockito.doNothing().when(transport).close();
        return session;
    }

}
