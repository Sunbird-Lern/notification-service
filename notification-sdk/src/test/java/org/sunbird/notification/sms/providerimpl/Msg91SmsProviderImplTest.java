package org.sunbird.notification.sms.providerimpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.GetRequest;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
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
import org.sunbird.notification.beans.MessageResponse;
import org.sunbird.notification.beans.OTPRequest;
import org.sunbird.notification.utils.NotificationConstant;
import org.sunbird.notification.utils.Util;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        Util.class,
        HttpClients.class,
        StatusLine.class,
        Unirest.class})
@PowerMockIgnore({
        "javax.management.*",
        "javax.net.ssl.*",
        "javax.security.*",
        "jdk.internal.reflect.*",
        "javax.crypto.*",
        "javax.script.*",
        "javax.xml.*",
        "com.sun.org.apache.xerces.*",
        "org.xml.*"
})
public class Msg91SmsProviderImplTest {

    ObjectMapper mapper = new ObjectMapper() ;
    @Before
    public void setUp() throws IOException, UnirestException {
        PowerMockito.mockStatic(Util.class);
        PowerMockito.when(Util.readValue(Mockito.eq(NotificationConstant.SUNBIRD_MSG_91_BASEURL))).thenReturn("http://sms");
        PowerMockito.when(Util.readValue(Mockito.eq(NotificationConstant.SUNBIRD_MSG_91_POST_URL))).thenReturn("http://sms");
        PowerMockito.when(Util.readValue(Mockito.eq(NotificationConstant.SUNBIRD_MSG_91_ROUTE))).thenReturn("http://sms");
        PowerMockito.when(Util.readValue(Mockito.eq(NotificationConstant.SUNBIRD_DEFAULT_COUNTRY_CODE))).thenReturn("91");

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
        HttpResponse response1  = Mockito.mock(HttpResponse.class);
        Mockito.when(response1.getStatus()).thenReturn(NotificationConstant.SUCCESS_CODE);
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setType(NotificationConstant.SUCCESS);
        messageResponse.setMessage("OTP verification");
        messageResponse.setCode("1234");
        Mockito.when(response1.getBody()).thenReturn(mapper.writeValueAsString(messageResponse));
        Mockito.when(getRequest.header(Mockito.anyString(),Mockito.anyString())).thenReturn(getRequest);
        Mockito.when(getRequest.asString()).thenReturn((HttpResponse) response1);

        PowerMockito.when(Unirest.get(Mockito.anyString())).thenReturn(getRequest);



    }
    @Test
    public void sendSmsSuccess(){

        Msg91SmsProviderImpl msg91SmsProvider = new Msg91SmsProviderImpl("123213213","#!32131313");
        Assert.assertTrue(msg91SmsProvider.sendSms("91+7829041789","13123213123131313", new HashMap<>()));
    }
    @Test
    public void sendSmsWithCountryCodeSuccess(){

        Msg91SmsProviderImpl msg91SmsProvider = new Msg91SmsProviderImpl("123213213","#!32131313");
        Assert.assertTrue(msg91SmsProvider.sendSms("782321789","13123213123131313","91", new HashMap<>()));
    }

    @Test
    public void sendBulkSmsWithCountryCodeSuccess(){

        Msg91SmsProviderImpl msg91SmsProvider = new Msg91SmsProviderImpl("123213213","#!32131313");
        Assert.assertTrue(msg91SmsProvider.bulkSms(Arrays.asList("7829041789"),"13123213123131313", new HashMap<>()));
    }
    @Test
    public void sendOTPSuccess(){
        OTPRequest otpRequest = new OTPRequest("3222122228","91",4,12312323,"verify","1235");
        Msg91SmsProviderImpl provider = new Msg91SmsProviderImpl("123213213","#!32131313");
        Assert.assertTrue(provider.sendOtp(otpRequest,new HashMap<>()));
    }

    @Test
    public void resendOTPSuccess(){
        OTPRequest otpRequest = new OTPRequest("3222122228","91",4,12312323,"verify","1235");
        Msg91SmsProviderImpl provider = new Msg91SmsProviderImpl("123213213","#!32131313");
        Assert.assertTrue(provider.resendOtp(otpRequest,new HashMap<>()));

    }

    @Test
    public void verifyOTPSuccess(){
        OTPRequest otpRequest = new OTPRequest("3222122228","91",4,12312323,"verify","1235");
        Msg91SmsProviderImpl provider = new Msg91SmsProviderImpl("123213213","#!32131313");
        Assert.assertTrue(provider.verifyOtp(otpRequest,new HashMap<>()));

    }
}
