/** */
package org.sunbird.notification.dispatcher;

import org.sunbird.notification.beans.EmailConfig;
import org.sunbird.notification.beans.EmailRequest;
import org.sunbird.notification.beans.SMSConfig;
import org.sunbird.notification.dispatcher.NotificationRouter.DeliveryMode;
import org.sunbird.notification.dispatcher.NotificationRouter.DeliveryType;
import org.sunbird.notification.email.service.IEmailService;
import org.sunbird.notification.email.service.impl.IEmailProviderFactory;
import org.sunbird.notification.sms.provider.ISmsProvider;
import org.sunbird.notification.sms.providerimpl.Msg91SmsProviderFactory;
import org.sunbird.pojo.NotificationRequest;
import org.sunbird.common.response.Response;
import org.sunbird.util.Constant;

import java.util.Map;

/** @author manzarul */
public class SyncMessageDispatcher {

  private IEmailService emailservice;
  private ISmsProvider smsProvider;

  public Response syncDispatch(NotificationRequest notification, Map<String,Object> context) {
    if (notification.getMode().equalsIgnoreCase(DeliveryMode.phone.name())
        && notification.getDeliveryType().equalsIgnoreCase(DeliveryType.message.name())) {
      return syncMessageDispatch(notification, context);
    }

    return syncEmailDispatch(notification, context);
  }

  private Response syncEmailDispatch(NotificationRequest notificationRequest, Map<String,Object> context) {
    EmailRequest request =
        new EmailRequest(
            notificationRequest.getConfig().getSubject(),
            notificationRequest.getIds(),
            null,
            null,
            null,
            notificationRequest.getTemplate().getData(),
            null);
    boolean emailResponse = getEmailInstance().sendEmail(request, context);
    Response response = new Response();
    response.put(Constant.RESPONSE, emailResponse);
    return response;
  }

  private Response syncMessageDispatch(NotificationRequest notificationRequest, Map<String,Object> context) {
    Response response = new Response();
    boolean smsResponse =
        getSmsInstance()
            .bulkSms(notificationRequest.getIds(), notificationRequest.getTemplate().getData(), context);
    response.put(Constant.RESPONSE, smsResponse);
    return response;
  }

  private ISmsProvider getSmsInstance() {
    if (smsProvider == null) {
      Msg91SmsProviderFactory factory = new Msg91SmsProviderFactory();
      SMSConfig config = new SMSConfig();
      smsProvider = factory.create(config);
    }
    return smsProvider;
  }

  private IEmailService getEmailInstance() {
    if (emailservice == null) {
      IEmailProviderFactory factory = new IEmailProviderFactory();
      EmailConfig config = new EmailConfig();
      emailservice = factory.create(config);
    }
    return emailservice;
  }
}
