package utils.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.ResponseHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.sunbird.common.exception.AuthorizationException;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.request.HeaderParam;
import org.sunbird.request.LoggerUtil;
import org.sunbird.util.SystemConfigUtil;
import org.sunbird.utils.PropertiesCache;
import play.http.ActionCreator;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import controllers.JsonKey;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class OnRequestHandler implements ActionCreator {
    private static LoggerUtil logger = new LoggerUtil(OnRequestHandler.class);
    private ObjectMapper mapper = new ObjectMapper();
    private static String custodianOrgHashTagId;

    @Override
    public Action createAction(Http.Request request, Method method) {
        return new Action.Simple() {
            @Override
            public CompletionStage<Result> call(Http.Request request) {
                Optional<String> optionalMessageId = request.getHeaders().get(JsonKey.REQUEST_MESSAGE_ID);
                String requestId;
                if (optionalMessageId.isPresent()) {
                    requestId = optionalMessageId.get();
                } else {
                    UUID uuid = UUID.randomUUID();
                    requestId = uuid.toString();
                }
                Optional<String> optionalTraceId =
                        request.getHeaders().get(HeaderParam.X_REQUEST_ID.getName());
                if (optionalTraceId.isPresent()) {
                    MDC.put(
                            JsonKey.X_REQUEST_ID,
                            request.getHeaders().get(HeaderParam.X_REQUEST_ID.getName()).get());
                } else {
                    MDC.put(JsonKey.X_REQUEST_ID, requestId);
                }
                CompletionStage<Result> result;
                Map userAuthentication = RequestInterceptor.verifyRequestData(request);
                String message = (String) userAuthentication.get(JsonKey.USER_ID);
                logger.info("API access by user:"+message);
                if (userAuthentication.get(JsonKey.MANAGED_FOR) != null) {
                    request =
                            request.addAttr(
                                    Attrs.MANAGED_FOR, (String) userAuthentication.get(JsonKey.MANAGED_FOR));
                }
                request = initializeContext(request, message, requestId);
                if (!JsonKey.USER_UNAUTH_STATES.contains(message)) {
                    request = request.addAttr(Attrs.USERID, message);
                    result = delegate.call(request);
                } else if (JsonKey.UNAUTHORIZED.equals(message)) {
                    result = getAuthorizedResult(request);
                } else {
                    result = delegate.call(request);
                }

                return result.thenApply(res -> res.withHeader("Access-Control-Allow-Origin", "*"));
            }
        };
    }

    /**
     *  Set Error code specific to operation
     * @param request
     *
     * @return
     */
    public CompletionStage<Result> getAuthorizedResult(Http.Request request)  {

      ResponseCode responseCode = ResponseCode.unAuthorized;
        Result result = null;
        try {
            result = ResponseHandler.handleFailureResponse(new AuthorizationException.NotAuthorized(responseCode), request);
        } catch (BaseException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(result);
    }


    /**
     * Set the Context paramter to the request
     *
     * @param httpReq
     * @param userId
     */
    Http.Request initializeContext(Http.Request httpReq, String userId, String requestId)  {
        Map<String, Object> requestContext = new WeakHashMap<>();
        try {
            String env = JsonKey.NOTIFICATION;
            requestContext.put(JsonKey.ENV, env);
            requestContext.put(JsonKey.REQUEST_TYPE, JsonKey.API_CALL);
            requestContext.put(JsonKey.URL, httpReq.uri());
            requestContext.put(JsonKey.METHOD, httpReq.method());
            Optional<String> optionalChannel = httpReq.getHeaders().get(HeaderParam.CHANNEL_ID.getName());
            String channel = null;
            if (optionalChannel.isPresent()) {
                channel = optionalChannel.get();
            }else {
                channel = getCustodianOrgHashTagId();
            }
            requestContext.put(JsonKey.CHANNEL, channel);
            requestContext.put(JsonKey.REQUEST_ID, requestId);
            requestContext.put(JsonKey.REQUEST_MESSAGE_ID, requestId);
            Optional<String> optionalAppId = httpReq.getHeaders().get(HeaderParam.X_APP_ID.getName());
            if (optionalAppId.isPresent()) {
                requestContext.put(JsonKey.APP_ID, optionalAppId.get());
            }
            requestContext.putAll(cacheTelemetryPdata());

            Optional<String> optionalDeviceId =
                    httpReq.getHeaders().get(HeaderParam.X_Device_ID.getName());
            if (optionalDeviceId.isPresent()) {
                requestContext.put(JsonKey.DEVICE_ID, optionalDeviceId.get());
            }
            Optional<String> optionalTraceEnabled =
                    httpReq.getHeaders().get(HeaderParam.X_TRACE_ENABLED.getName());
            if (optionalTraceEnabled.isPresent()) {
                requestContext.put(JsonKey.X_TRACE_ENABLED, optionalTraceEnabled.get());
            }
            Optional<String> optionalTraceId =
                    httpReq.getHeaders().get(HeaderParam.X_REQUEST_ID.getName());
            if (optionalTraceId.isPresent()) {
                requestContext.put(JsonKey.X_REQUEST_ID, optionalTraceId.get());
                httpReq = httpReq.addAttr(Attrs.X_REQUEST_ID, optionalTraceId.get());
            } else {
                httpReq = httpReq.addAttr(Attrs.X_REQUEST_ID, requestId);
                requestContext.put(JsonKey.X_REQUEST_ID, requestId);
            }
            if (null != userId && !JsonKey.USER_UNAUTH_STATES.contains(userId)) {
                requestContext.put(JsonKey.ACTOR_ID, userId);
                requestContext.put(JsonKey.ACTOR_TYPE, StringUtils.capitalize(JsonKey.USER));
            } else {
                Optional<String> optionalConsumerId =
                        httpReq.getHeaders().get(HeaderParam.X_Consumer_ID.getName());
                String consumerId;
                if (optionalConsumerId.isPresent()) {
                    consumerId = optionalConsumerId.get();
                } else {
                    consumerId = JsonKey.DEFAULT_CONSUMER_ID;
                }
                requestContext.put(JsonKey.ACTOR_ID, consumerId);
                requestContext.put(JsonKey.ACTOR_TYPE, StringUtils.capitalize(JsonKey.CONSUMER));
            }
            Map<String, Object> map = new WeakHashMap<>();
            map.put(JsonKey.CONTEXT, requestContext);
            return httpReq.addAttr(Attrs.CONTEXT, mapper.writeValueAsString(map));
        } catch (Exception ex) {
            logger.error("Error process set request context" , ex);
        }
        return httpReq;
    }

    private String getCustodianOrgHashTagId() {
        if (null != custodianOrgHashTagId) {
            return custodianOrgHashTagId;
        }
        synchronized (OnRequestHandler.class) {
            if (custodianOrgHashTagId == null) {
                try {
                    // Get hash tag ID of custodian org
                    Map<String, Object> custodianOrgDetails = SystemConfigUtil.getCustodianOrgDetails();
                    if (null != custodianOrgDetails && !custodianOrgDetails.isEmpty()) {
                        custodianOrgHashTagId = (String) custodianOrgDetails.get(JsonKey.HASH_TAG_ID);
                    } else {
                        custodianOrgHashTagId = "";
                    }

                } catch (Exception ex) {
                    custodianOrgHashTagId = "";
                }
            }
        }
        return custodianOrgHashTagId;
    }
    private static Map<String, Object> cacheTelemetryPdata() {
        Map<String, Object> telemetryPdata = new HashMap<>();
        telemetryPdata.put("telemetry_pdata_id", PropertiesCache.getConfigValue("telemetry_pdata_id"));
        telemetryPdata.put(
                "telemetry_pdata_pid", PropertiesCache.getConfigValue("telemetry_pdata_pid"));
        telemetryPdata.put(
                "telemetry_pdata_ver", PropertiesCache.getConfigValue("telemetry_pdata_ver"));
        return telemetryPdata;
    }

}
