package org.sunbird.notification.actor;

import akka.actor.Props;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.notification.dispatcher.NotificationRouter;
import org.sunbird.pojo.NotificationRequest;
import org.sunbird.utils.PropertiesCache;

import java.util.Arrays;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        SystemConfigUtil.class,
        PropertiesCache.class
})
public class NotificationActorTest extends BaseActorTest{

    public  final Props props = Props.create(NotificationActor.class);


    private NotificationRequest getNotificationRequest(){
          NotificationRequest request = new NotificationRequest();
          request.setIds(Arrays.asList("123454321"));
          request.setMode("email");
          request.setDeliveryType(NotificationRouter.DeliveryType.message.name());
    }
}
