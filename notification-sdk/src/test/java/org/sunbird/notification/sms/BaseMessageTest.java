package org.sunbird.notification.sms;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.RequestBodyEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.AdditionalMatchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.utils.PropertiesCache;
import org.sunbird.notification.beans.SMSConfig;
import org.sunbird.notification.sms.provider.ISmsProvider;
import org.sunbird.notification.sms.providerimpl.Msg91SmsProviderFactory;
import org.sunbird.notification.sms.providerimpl.Msg91SmsProviderImpl;
import org.sunbird.notification.utils.SMSFactory;

import static org.powermock.api.mockito.PowerMockito.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*", "javax.security.*", "jdk.internal.reflect.*"})
@PrepareForTest({HttpClients.class, PropertiesCache.class, Unirest.class, GetRequest.class, SMSFactory.class, Msg91SmsProviderFactory.class, Msg91SmsProviderImpl.class, SMSConfig.class,System.class})
public abstract class BaseMessageTest {
  public static PropertiesCache propertiesCache;

  @BeforeClass
  public static void initMockRules() {
    PowerMockito.mockStatic(SMSFactory.class);
    PowerMockito.mockStatic(Msg91SmsProviderFactory.class);
    PowerMockito.mockStatic(System.class);
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    CloseableHttpResponse httpResp = mock(CloseableHttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);
    GetRequest getRequest = mock(GetRequest.class);
    HttpRequestWithBody requestWithBody = mock(HttpRequestWithBody.class);
    RequestBodyEntity requestBodyEntity = mock(RequestBodyEntity.class);
    HttpResponse<String> httpResponse = mock(HttpResponse.class);
    PowerMockito.mockStatic(HttpClients.class);
    PowerMockito.mockStatic(Unirest.class);
    try {
      doReturn(httpClient).when(HttpClients.class, "createDefault");
      doReturn(httpResp).when(httpClient).execute(Mockito.any(HttpPost.class));
      doReturn(statusLine).when(httpResp).getStatusLine();
      doReturn(200).when(statusLine).getStatusCode();
      PowerMockito.when(Unirest.get("https://control.msg91.com")).thenReturn(getRequest);
      PowerMockito.when(getRequest.asString()).thenReturn(httpResponse);
      PowerMockito.when(httpResponse.getStatus()).thenReturn(Integer.valueOf(200));
      PowerMockito.when(httpResponse.getBody())
          .thenReturn(
              "{\n"
                  + "  \"message\":\"3763646c3058373530393938\",\n"
                  + "  \"type\":\"success\"\n"
                  + "}\n"
                  + "");
    } catch (Exception e) {
      Assert.fail("Exception while mocking static " + e.getLocalizedMessage());
    }

    PowerMockito.mockStatic(PropertiesCache.class);
    propertiesCache = mock(PropertiesCache.class);
    when(PropertiesCache.getInstance()).thenReturn(propertiesCache);

    doReturn("randomString").when(propertiesCache).getProperty(Mockito.anyString());
    when(System.getenv(Mockito.anyString())).thenReturn("randomString");
    ISmsProvider msg91SmsProvider = PowerMockito.mock(Msg91SmsProviderImpl.class);
    PowerMockito.when(SMSFactory.getInstance(Mockito.anyString(), Mockito.any(SMSConfig.class))).thenReturn(msg91SmsProvider);
  }


}
