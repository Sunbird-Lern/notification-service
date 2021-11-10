package util;

import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.util.SystemConfigUtil;
import org.sunbird.utils.HttpClientUtil;

import javax.annotation.meta.When;


@RunWith(PowerMockRunner.class)
@PrepareForTest({
        HttpClientUtil.class

})
public class SystemConfigUtilTest {

    @Test
    public void initSuccess(){
        PowerMockito.mockStatic(HttpClientUtil.class);
        Mockito.when(HttpClientUtil.get(Mockito.anyString(),Mockito.anyMap(),Mockito.any())).thenReturn(getSystemConfigResponse());
        Mockito.when(HttpClientUtil.post(Mockito.anyString(),Mockito.any(),Mockito.anyMap(),Mockito.any())).thenReturn(getCustodianOrgResponse());
        SystemConfigUtil.init();
        Assert.assertTrue(true);
    }

    private String getSystemConfigResponse() {
        String response ="{\"id\":\"api.system.settings.list\",\"ver\":\"v1\",\"ts\":\"2021-09-30 16:12:13:549+0000\",\"params\":{\"resmsgid\":null,\"msgid\":\"eb0eeee68ee191339831d20cde202f55\",\"err\":null,\"status\":\"success\",\"errmsg\":null},\"responseCode\":\"OK\",\"result\":{\"response\":[{\"id\":\"custodianRootOrgId\",\"field\":\"custodianRootOrgId\",\"value\":\"0126796199493140480\"}]}}";
        return response;
    }
    private String getCustodianOrgResponse() {
        String response ="{\"id\":\"api.system.settings.list\",\"ver\":\"v1\",\"ts\":\"2021-09-30 16:12:13:549+0000\",\"params\":{\"resmsgid\":null,\"msgid\":\"eb0eeee68ee191339831d20cde202f55\",\"err\":null,\"status\":\"success\",\"errmsg\":null},\"responseCode\":\"OK\",\"result\":{\"response\":{\"id\":\"custodianRootOrgId\",\"name\":\"test\"}}}";
        return response;
    }
}
