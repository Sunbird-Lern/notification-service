/** */
package org.sunbird.notification.dispatcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.sunbird.common.exception.ActorServiceException;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.IUserResponseMessage;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.response.Response;
import org.sunbird.notification.beans.Constants;
import org.sunbird.notification.beans.OTPRequest;
import org.sunbird.notification.beans.SMSConfig;
import org.sunbird.notification.dispatcher.impl.FCMNotificationDispatcher;
import org.sunbird.notification.sms.provider.ISmsProvider;
import org.sunbird.notification.sms.providerimpl.Msg91SmsProviderFactory;
import org.sunbird.notification.utils.NotificationConstant;
import org.sunbird.notification.utils.Util;
import org.sunbird.pojo.Config;
import org.sunbird.pojo.NotificationRequest;
import org.sunbird.pojo.OTP;
import org.sunbird.request.LoggerUtil;
import org.sunbird.util.Constant;

import java.io.IOException;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @author manzarul
 */
public class NotificationRouter {
  private static LoggerUtil logger = new LoggerUtil(NotificationRouter.class);
  private static final String TEMPLATE_SUFFIX = ".vm";
  private SyncMessageDispatcher syDispatcher = new SyncMessageDispatcher();
  private ObjectMapper mapper = new ObjectMapper();

  public enum DeliveryMode {
    phone,
    email,
    device;
  }

  public enum DeliveryType {
    message,
    otp,
    whatsapp,
    call;
  }

  private ISmsProvider smsProvider = null;

  public ISmsProvider getSMSInstance() {
    if (smsProvider == null) {
      SMSConfig config = new SMSConfig(System.getenv(NotificationConstant.SUNBIRD_MSG_91_AUTH), "");
      Msg91SmsProviderFactory mesg91ObjectFactory = new Msg91SmsProviderFactory();
      smsProvider = mesg91ObjectFactory.create(config);
    }
    return smsProvider;
  }

  public Response route(
      List<NotificationRequest> notificationRequestList, boolean isDryRun, boolean isSync, Map<String,Object> context)
      throws BaseException {
    logger.info(context, "making call to route method");
    Response response = new Response();
    if (CollectionUtils.isNotEmpty(notificationRequestList)) {
      Map<String, Object> responseMap = new HashMap<String, Object>();
      for (NotificationRequest notification : notificationRequestList) {
        if (notification.getMode().equalsIgnoreCase(DeliveryMode.phone.name())
            && (notification.getDeliveryType().equalsIgnoreCase(DeliveryType.message.name())
                || notification.getDeliveryType().equalsIgnoreCase(DeliveryType.otp.name()))) {
          response = handleMessageAndOTP(notification, isDryRun, responseMap, isSync, context);
        } else if (notification.getMode().equalsIgnoreCase(DeliveryMode.device.name())) {
          response = writeDataToKafka(notification, response, isDryRun, responseMap, isSync, context);
        } else if (notification.getMode().equalsIgnoreCase(DeliveryMode.email.name())
            && notification.getDeliveryType().equalsIgnoreCase(DeliveryType.message.name())) {
          String message = null;
          if (notification.getTemplate() != null
              && StringUtils.isNotBlank(notification.getTemplate().getData())) {
            message =
                getMessage(
                    notification.getTemplate().getData(), notification.getTemplate().getParams(), context);
            notification.getTemplate().setData(message);
          } else if (notification.getTemplate() != null
              && StringUtils.isNotBlank(notification.getTemplate().getId())) {
            String data = createNotificationBody(notification, context);
            notification.getTemplate().setData(data);
          }
          if (isSync) {
            response = syDispatcher.syncDispatch(notification,  context);
          } else {
            response = writeDataToKafka(notification, response, isDryRun, responseMap, isSync, context);
          }
        }
      }
    } else {
      logger.info(context,
          "requested notification list is either null or empty :" + notificationRequestList);
    }
    return response;
  }

  private Response handleMessageAndOTP(
      NotificationRequest notification,
      boolean isDryRun,
      Map<String, Object> responseMap,
      boolean isSync,
      Map<String,Object> context)
      throws BaseException {
    Response response = new Response();
    String message = null;
    if (notification.getTemplate() != null
        && StringUtils.isNotBlank(notification.getTemplate().getData())) {
      message =
          getMessage(notification.getTemplate().getData(), notification.getTemplate().getParams(), context);
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
                IResponseMessage.Message.INVALID_REQUESTED_DATA, NotificationConstant.OTP_PHONE_ERROR),
            ResponseCode.CLIENT_ERROR.getCode());
      }
      OTPRequest request =
          new OTPRequest(ids.get(0), null, otp.getLength(), otp.getExpiryInMinute(), message, null);
      boolean smsResponse = getSMSInstance().sendOtp(request, context);
      responseMap.put(ids.get(0), smsResponse);
      response.putAll(responseMap);
    } else {
      if (notification.getTemplate() != null) {
        notification.getTemplate().setData(message);
      }
      if (isSync) {
        response = syDispatcher.syncDispatch(notification,  context);

      } else {
        response = writeDataToKafka(notification, response, isDryRun, responseMap, isSync, context);
      }
    }
    return response;
  }

  private String createNotificationBody(NotificationRequest notification, Map<String,Object> context) throws BaseException {
    return readVm(notification.getTemplate().getId(), notification.getTemplate().getParams(), context);
  }

  private String readVm(String templateName, JsonNode node, Map<String,Object> requestContext) throws BaseException {
    VelocityEngine engine = new VelocityEngine();
    VelocityContext context = getContextObj(node);
    Properties p = new Properties();
    p.setProperty("resource.loader", "class");
    p.setProperty(
        "class.resource.loader.class",
        "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    StringWriter writer = null;
    String body = null;
    try {
      engine.init(p);
      Template template = engine.getTemplate(templateName + TEMPLATE_SUFFIX);
      writer = new StringWriter();
      template.merge(context, writer);
      body = writer.toString();
    } catch (Exception e) {
      logger.error(requestContext,"Failed to load velocity template =" + templateName + " " + e.getMessage(), e);
      throw new ActorServiceException.InvalidRequestData(
          IUserResponseMessage.TEMPLATE_NOT_FOUND,
          MessageFormat.format(
              IResponseMessage.Message.INVALID_REQUESTED_DATA,
              NotificationConstant.EMAIL_TEMPLATE_NOT_FOUND),
          ResponseCode.CLIENT_ERROR.getCode());
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          logger.error(requestContext,"Failed to closed writer object =" + e.getMessage(), e);
        }
      }
    }
    return body;
  }

  public Response verifyOtp(OTPRequest otpRequest, Map<String,Object> context) {
    boolean verificationResp = getSMSInstance().verifyOtp(otpRequest, context);
    Response response = new Response();
    if (verificationResp) {
      response.put(NotificationConstant.MESSAGE, NotificationConstant.SUCCESS);
    } else {
      response.put(NotificationConstant.MESSAGE, NotificationConstant.FAILURE);
    }
    return response;
  }

  private Response writeDataToKafka(
      NotificationRequest notification,
      Response response,
      boolean isDryRun,
      Map<String, Object> responseMap,
      boolean isSync,
      Map<String,Object> context) {
    FCMNotificationDispatcher.getInstance().dispatch(notification, isDryRun, isSync, context);
    logger.info(context, "Got response from FCM ");
    responseMap.put(Constant.RESPONSE, NotificationConstant.SUCCESS);
    response.putAll(responseMap);
    return response;
  }

  private String getMessage(String message, JsonNode node, Map<String,Object> requestContext) {
    VelocityContext context = new VelocityContext();
    if (node != null) {
      Map<String, String> paramValue = mapper.convertValue(node, Map.class);
      Iterator<Entry<String, String>> itr = paramValue.entrySet().iterator();
      while (itr.hasNext()) {
        Entry<String, String> entry = itr.next();
        if (null != entry.getValue()) {
          context.put(entry.getKey(), entry.getValue());
        }
      }
    }
    StringWriter writer = null;
    try {
      Velocity.init();
      writer = new StringWriter();
      Velocity.evaluate(context, writer, "SimpleVelocity", message);
    } catch (Exception e) {
      logger.error(requestContext,
        "NotificationRouter:getMessage : Exception occurred with message =" + e.getMessage(), e);
    }
    return writer.toString();
  }

  private VelocityContext getContextObj(JsonNode node) {
    VelocityContext context = null;
    if (node != null) {
      context = new VelocityContext(mapper.convertValue(node, Map.class));
    } else {
      context = new VelocityContext();
    }
    if (!context.containsKey(Constants.FROM_EMAIL)) {
      context.put(Constants.FROM_EMAIL, Util.readValue(Constants.EMAIL_SERVER_FROM));
    }
    if (!context.containsKey("orgImageUrl")) {
      context.put("orgImageUrl", Util.readValue(Constants.EMAIL_SERVER_FROM));
    }
    return context;
  }
}
