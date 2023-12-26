package util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.util.ConfigUtil;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        ConfigFactory.class
})
public class ConfigUtilTest {

       @Before
       public void testConfig(){
           PowerMockito.mockStatic(ConfigFactory.class);
           Config config = Mockito.mock(Config.class);
           PowerMockito.when(ConfigFactory.load(Mockito.anyString())).thenReturn(config);
           PowerMockito.when(ConfigFactory.systemEnvironment()).thenReturn(config);
           Mockito.when(config.withFallback(config)).thenReturn(config);
       }
/*
     @Test
     public void testConfigTest(){
           Config result = ConfigUtil.getConfig("test");
         Assert.assertTrue(result != null);
     }

    @Test
    public void validateMandatoryConfigValueFailed(){
       boolean flag =false;
       try{
           ConfigUtil.validateMandatoryConfigValue("");
       }catch (Exception ex){
           flag=true;
       }

       Assert.assertTrue(true);
    }*/

    @Test
    public void getConfigJsonString(){
        PowerMockito.mockStatic(ConfigFactory.class);
        Config config = Mockito.mock(Config.class);
        PowerMockito.when(ConfigFactory.parseString(Mockito.anyString())).thenReturn(config);
        Config newConfig = ConfigUtil.getConfigFromJsonString("[{'element':'value'}]","");
        Assert.assertTrue(null != newConfig);
    }

}
