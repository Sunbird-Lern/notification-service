/** */
package org.sunbird.notification.dispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sunbird.notification.beans.OTPRequest;
import org.sunbird.notification.beans.SMSConfig;
import org.sunbird.notification.dispatcher.impl.FCMNotificationDispatcher;
import org.sunbird.notification.sms.provider.ISmsProvider;
import org.sunbird.notification.sms.providerimpl.Msg91SmsProviderFactory;
import org.sunbird.notification.utils.FCMResponse;
import org.sunbird.notification.utils.NotificationConstant;
import org.sunbird.pojo.Config;
import org.sunbird.pojo.NotificationRequest;
import org.sunbird.pojo.OTP;
import org.sunbird.response.Response;
import org.sunbird.util.Constant;

/**
 * mojut6de4rnj,
 *
 * @author manzarul
 */
public class NotificationRouter {
  private static Logger logger = LogManager.getLogger(NotificationRouter.class);

  enum DeliveryMode {
    phone,
    email,
    device;
  }

  enum DeliveryType {
    message,
    otp,
    whatsapp,
    call;
  }

  Msg91SmsProviderFactory mesg91ObjectFactory = new Msg91SmsProviderFactory();
  private ISmsProvider smsProvider = null;
  INotificationDispatcher FcmDispatcher = new FCMNotificationDispatcher();
  ObjectMapper mapper = new ObjectMapper();

  private ISmsProvider getSMSInstance() {
    if (smsProvider == null) {
      SMSConfig config = new SMSConfig(System.getenv(NotificationConstant.SUNBIRD_MSG_91_AUTH), "");
      smsProvider = mesg91ObjectFactory.create(config);
    }
    return smsProvider;
  }

  public Response route(List<NotificationRequest> notificationRequestList, boolean isDryRun) {
    logger.info("making call to route method");
    Response response = new Response();
    if (CollectionUtils.isNotEmpty(notificationRequestList)) {
      Map<String, Object> responseMap = new HashMap<String, Object>();
      for (NotificationRequest notification : notificationRequestList) {
        if (notification.getMode().equalsIgnoreCase(DeliveryMode.phone.name())
            && (notification.getDeliveryType().equalsIgnoreCase(DeliveryType.message.name())
                || notification.getDeliveryType().equalsIgnoreCase(DeliveryType.otp.name()))) {
          Config config = notification.getConfig();
          if (config.getOtp() != null) {
            OTP otp = config.getOtp();
            int expiryTime = otp.getExpiry() == null ? 0 : Integer.parseInt(otp.getExpiry());
            List<String> ids = notification.getIds();
            for (int i = 0; i < ids.size(); i++) {
              OTPRequest request =
                  new OTPRequest(
                      ids.get(i),
                      NotificationConstant.SUNBIRD_DEFAULT_COUNTRY_CODE,
                      otp.getLength(),
                      expiryTime,
                      null,
                      null);
              boolean smsResponse = getSMSInstance().sendOtp(request);
              responseMap.put(ids.get(i), smsResponse);
            }
            response.putAll(responseMap);
          } else {

          }

        } else if (notification.getMode().equalsIgnoreCase(DeliveryMode.device.name())) {
          FCMResponse responses = FcmDispatcher.dispatch(notification, isDryRun);
          logger.info("response from FCM " + responses);
          responseMap.put(Constant.RESPONSE, NotificationConstant.SUCCESS);
          response.putAll(responseMap);
        } else {
          // Not implemented yet.
        }
      }
    }
    return response;
  }
}
