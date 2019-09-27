/** */
package org.sunbird.notification.dispatcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sunbird.ActorServiceException;
import org.sunbird.BaseException;
import org.sunbird.message.IResponseMessage;
import org.sunbird.message.IUserResponseMessage;
import org.sunbird.message.ResponseCode;
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

  public Response route(List<NotificationRequest> notificationRequestList, boolean isDryRun)
      throws BaseException {
    logger.info("making call to route method");
    Response response = new Response();
    if (CollectionUtils.isNotEmpty(notificationRequestList)) {
      Map<String, Object> responseMap = new HashMap<String, Object>();
      for (NotificationRequest notification : notificationRequestList) {
        if (notification.getMode().equalsIgnoreCase(DeliveryMode.phone.name())
            && (notification.getDeliveryType().equalsIgnoreCase(DeliveryType.message.name())
                || notification.getDeliveryType().equalsIgnoreCase(DeliveryType.otp.name()))) {
          String message = null;
          if (notification.getTemplate() != null
              && StringUtils.isNotBlank(notification.getTemplate().getData())) {
            message =
                getMessage(
                    notification.getTemplate().getData(), notification.getTemplate().getParams());
          }

          Config config = notification.getConfig();
          if (config != null && config.getOtp() != null
              || notification.getDeliveryType().equalsIgnoreCase(DeliveryType.otp.name())) {
            OTP otp = config.getOtp();
            List<String> ids = notification.getIds();
            if (ids.size() > 1) {
              throw new ActorServiceException.InvalidRequestData(
                  IUserResponseMessage.USER_NOT_FOUND,
                  MessageFormat.format(
                      IResponseMessage.INVALID_REQUESTED_DATA,
                      NotificationConstant.OTP_PHONE_ERROR),
                  ResponseCode.CLIENT_ERROR.getCode());
            }
            OTPRequest request =
                new OTPRequest(
                    ids.get(0),
                    NotificationConstant.SUNBIRD_DEFAULT_COUNTRY_CODE,
                    otp.getLength(),
                    otp.getExpiryInMinute(),
                    message,
                    null);
            boolean smsResponse = getSMSInstance().sendOtp(request);
            responseMap.put(ids.get(0), smsResponse);
            response.putAll(responseMap);
          } else {
            if (notification.getTemplate() != null) {
              notification.getTemplate().setData(message);
            }
            response = writeDataToKafa(notification, response, isDryRun, responseMap);
          }

        } else if (notification.getMode().equalsIgnoreCase(DeliveryMode.device.name())) {
          response = writeDataToKafa(notification, response, isDryRun, responseMap);
        } else {
          // Not implemented yet.
        }
      }
    } else {
      logger.info(
          "requested notification list is either null or empty :" + notificationRequestList);
    }
    return response;
  }

  private Response writeDataToKafa(
      NotificationRequest notification,
      Response response,
      boolean isDryRun,
      Map<String, Object> responseMap) {
    FCMResponse responses = FcmDispatcher.dispatch(notification, isDryRun);
    logger.info("response from FCM " + responses);
    responseMap.put(Constant.RESPONSE, NotificationConstant.SUCCESS);
    response.putAll(responseMap);
    return response;
  }

  private String getMessage(String message, JsonNode node) {
    if (node != null) {
      Map<String, String> paramValue = mapper.convertValue(node, Map.class);
      Iterator<Entry<String, String>> itr = paramValue.entrySet().iterator();
      while (itr.hasNext()) {
        Entry<String, String> entry = itr.next();
        message = message.replace("$" + entry.getKey(), entry.getValue());
      }
    }
    return message;
  }
}
