package org.sunbird.notification.actor;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.BaseActor;
import org.sunbird.JsonKey;
import org.sunbird.NotificationValidator;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.request.Request;
import org.sunbird.notification.handler.FeedNotificationHandler;
import org.sunbird.notification.handler.INotificationHandler;
import org.sunbird.notification.handler.NotificationHandlerFactory;
import org.sunbird.pojo.NotificationV2Request;
import org.sunbird.request.LoggerUtil;
import org.sunbird.common.response.Response;
import org.sunbird.telemetry.TelemetryEnvKey;
import org.sunbird.telemetry.util.TelemetryUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ActorConfig(
        tasks = {JsonKey.CREATE_NOTIFICATION},
        asyncTasks = {},
        dispatcher= "notification-dispatcher"
)
public class CreateNotificationActor extends BaseActor {
    private static LoggerUtil logger = new LoggerUtil(CreateNotificationActor.class);

    @Override
    public void onReceive(Request request) throws Throwable {
        logger.info(request.getRequest(),"Call started for onReceive method");
        boolean isSync = false;
        String version = (String) request.getRequest().get(JsonKey.VERSION);
        try {
            Response response = new Response();
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> notifications =
                    (List<Map<String, Object>>) request.getRequest().get(JsonKey.NOTIFICATIONS);

            String deliveryMode = request.getManagerName();
            if (StringUtils.isNotBlank(deliveryMode) && "sync".equalsIgnoreCase(deliveryMode)) {
                isSync = true;
            }
            if(JsonKey.V1.equals(version)){
                response = new FeedNotificationHandler().sendV1Notification(notifications.get(0),request.getContext());
            }else {
                List<NotificationV2Request> notificationRequestList = notifications.stream().map(x -> mapper.convertValue(x, NotificationV2Request.class))
                        .collect(Collectors.toList());
                for (NotificationV2Request notificationV2Request : notificationRequestList) {
                    NotificationValidator.validateMaxSupportedIds(notificationV2Request.getIds());
                    INotificationHandler handler = NotificationHandlerFactory.getNotificationHandler(notificationV2Request.getType());
                    response = handler.sendNotification(notificationV2Request, false, isSync, request.getContext());
                }
            }
            logger.info(request.getContext(), "response got from notification service " + response);
            sender().tell(response, getSelf());
        }catch (BaseException ex){
            logger.error(MessageFormat.format(":Error Msg: {0} ",ex.getMessage()),
                    ex);
            throw ex;
        } catch (Exception ex){
            logger.error(MessageFormat.format("CreateNotificationActor:Error Msg: {0} ",ex.getMessage()),
                    ex);
            throw new BaseException(IResponseMessage.Key.SERVER_ERROR,IResponseMessage.Message.INTERNAL_ERROR, ResponseCode.serverError.getResponseCode());
        }
       logTelemetry(request);
    }

    public static void logTelemetry(Request actorMessage){
        String source =
                actorMessage.getContext().get(org.sunbird.JsonKey.REQUEST_SOURCE) != null
                        ? (String) actorMessage.getContext().get(org.sunbird.JsonKey.REQUEST_SOURCE)
                        : "";
        List<Map<String, Object>> correlatedObject = new ArrayList<>();
        if (StringUtils.isNotBlank(source)) {
            TelemetryUtil.generateCorrelatedObject(
                    source, StringUtils.capitalize(org.sunbird.JsonKey.REQUEST_SOURCE), null, correlatedObject);
        }
        Map<String, Object> targetObject = null;
        targetObject = TelemetryUtil.generateTargetObject(UUID.randomUUID().toString(), TelemetryEnvKey.NOTIFICATION_CREATED, null, null);

        // Add user information to Cdata
        TelemetryUtil.generateCorrelatedObject(
                (String) actorMessage.getContext().get(JsonKey.USER_ID),
                TelemetryEnvKey.USER,
                null,
                correlatedObject);

        TelemetryUtil.telemetryProcessingCall(
                actorMessage.getRequest(), targetObject, correlatedObject, actorMessage.getContext());
    }
}

