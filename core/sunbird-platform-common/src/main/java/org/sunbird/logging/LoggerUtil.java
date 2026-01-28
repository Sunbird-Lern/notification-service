package org.sunbird.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.exception.ProjectCommonException;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.request.Request;
import org.sunbird.common.request.RequestContext;
import org.sunbird.exception.ResponseCode;
import org.sunbird.keys.JsonKey;
import org.sunbird.telemetry.util.TelemetryEvents;
import org.sunbird.telemetry.util.TelemetryWriter; // Updated import if needed, assuming TelemetryWriter is in same package or imported

import net.logstash.logback.marker.Markers;

import java.util.HashMap;
import java.util.Map;

public class LoggerUtil {

    private Logger logger;
    private String infoLevel = "INFO";
    private String debugLevel = "DEBUG";
    private String errorLevel = "ERROR";
    private String warnLevel = "WARN";
    private Logger defaultLogger;
    private final ObjectMapper mapper = new ObjectMapper();

    public LoggerUtil(Class c) {
        logger = LoggerFactory.getLogger(c);
        defaultLogger = LoggerFactory.getLogger("defaultLogger");
    }

    public void info(RequestContext requestContext, String message, Map<String, Object> object, Map<String, Object> param) {
        if (requestContext != null) {
            requestContext.setLoggerLevel(infoLevel);
            logger.info(jsonMapper(requestContext, message, object, param));
        } else defaultLogger.info(message);
    }

    public void info(Object requestContext, String message) {
        if (requestContext instanceof RequestContext) {
            info((RequestContext) requestContext, message, null, null);
        } else if (requestContext instanceof Map) {
            if (requestContext != null) {
                logger.info(Markers.appendEntries((Map) requestContext), message);
            } else {
                defaultLogger.info(message);
            }
        } else {
             defaultLogger.info(message);
        }
    }

    public void debug(RequestContext requestContext, String message, Map<String, Object> object, Map<String, Object> param) {
        if (isDebugEnabled(requestContext)) {
            requestContext.setLoggerLevel(debugLevel);
            logger.info(jsonMapper(requestContext, message, object, param));
        } else defaultLogger.debug(message);
    }

    public void debug(Object requestContext, String message) {
        if (requestContext instanceof RequestContext) {
            debug((RequestContext) requestContext, message, null, null);
        } else if (requestContext instanceof Map) {
             if (isDebugEnabled((Map) requestContext)) {
                 logger.info(Markers.appendEntries((Map) requestContext), message); 
             } else {
                 logger.debug(message);
             }
        } else {
             defaultLogger.debug(message);
        }
    }

    public void error(RequestContext requestContext, String message, Map<String, Object> object, Map<String, Object> param, Throwable e) {
        if (requestContext != null) {
            requestContext.setLoggerLevel(errorLevel);
            logger.error(jsonMapper(requestContext, message, object, param), e);
        } else defaultLogger.error(message, e);
    }

    public void error(RequestContext requestContext, String message, Map<String, Object> object, Map<String, Object> param, Throwable e, Map<String, Object> telemetryInfo) {
        if (requestContext != null) {
            requestContext.setLoggerLevel(errorLevel);
            logger.error(jsonMapper(requestContext, message, object, param), e);
        } else defaultLogger.error(message, e);
        telemetryProcess(requestContext, telemetryInfo, e);
    }

    public void error(Object requestContext, String message, Throwable e) {
        if (requestContext instanceof RequestContext) {
             error((RequestContext) requestContext, message, null, null, e);
        } else if (requestContext instanceof Map) {
             logger.error(Markers.appendEntries((Map) requestContext), message, e);
        } else {
             defaultLogger.error(message, e);
        }
    }

    public void error(Object requestContext, String message, Throwable e, Map<String, Object> telemetryInfo) {
        if (requestContext instanceof RequestContext) {
             error((RequestContext) requestContext, message, null, null, e, telemetryInfo);
        } else if (requestContext instanceof Map) {
             logger.error(Markers.appendEntries((Map) requestContext), message, e);
             telemetryProcess((Map) requestContext, e); // sb-utils specific call (takes Map context, Throwable)
        } else {
             defaultLogger.error(message, e);
             // Should we call telemetryProcess for null? sb-utils did.
             // But which one? Map version accepts null map?
             if (requestContext == null) telemetryProcess((Map) null, e); 
        }
    }

    public void warn(RequestContext requestContext, String message, Map<String, Object> object, Map<String, Object> param, Throwable e) {
        if (requestContext != null) {
            requestContext.setLoggerLevel(warnLevel);
            logger.warn((jsonMapper(requestContext, message, object, param)), e);
        } else defaultLogger.warn(message, e);
    }

    public void warn(Object requestContext, String message, Throwable e) {
        if (requestContext instanceof RequestContext) {
             warn((RequestContext) requestContext, message, null, null, e);
        } else if (requestContext instanceof Map) {
             logger.warn(Markers.appendEntries((Map) requestContext), message, e);
        } else {
             defaultLogger.warn(message, e);
        }
    }

    private static boolean isDebugEnabled(RequestContext requestContext) {
        return (null != requestContext && StringUtils.equalsIgnoreCase("true", requestContext.getDebugEnabled()));
    }
    
    private static boolean isDebugEnabled(Map<String,Object> requestContext) {
        return (null != requestContext
                && StringUtils.equalsIgnoreCase("true", (String)requestContext.get(JsonKey.DEBUG_ENABLED)));
    }

    private void telemetryProcess(RequestContext requestContext, Map<String, Object> telemetryInfo, Throwable e) {
        ProjectCommonException projectCommonException = null;
        if (e instanceof ProjectCommonException) {
            projectCommonException = (ProjectCommonException) e;
        } else {
            projectCommonException =
                    new ProjectCommonException(
                            ResponseCode.internalError.getErrorCode(),
                            ResponseCode.internalError.getErrorMessage(),
                            ResponseCode.SERVER_ERROR.getResponseCode());
        }
        Request request = new Request();
        request.setRequestContext(requestContext);
        telemetryInfo.put(JsonKey.TELEMETRY_EVENT_TYPE, TelemetryEvents.ERROR.getName());

        Map<String, Object> params = (Map<String, Object>) telemetryInfo.get(JsonKey.PARAMS);
        params.put(JsonKey.ERROR, projectCommonException.getErrorCode());
        params.put(JsonKey.STACKTRACE, generateStackTrace(e.getStackTrace()));
        request.setRequest(telemetryInfo);
        //		lmaxWriter.submitMessage(request);
        TelemetryWriter.write(request);
    }
    
    // sb-utils version
    private void telemetryProcess(Map<String,Object> requestContext, Throwable e) {
        BaseException baseException = null;
        if (e instanceof BaseException) {
            baseException = (BaseException) e;
        } else {
            baseException =
                    new BaseException(
                            org.sunbird.common.message.ResponseCode.SERVER_ERROR.getErrorCode(),
                            org.sunbird.common.message.ResponseCode.SERVER_ERROR.getErrorMessage(),
                            org.sunbird.common.message.ResponseCode.SERVER_ERROR.getResponseCode());
        }
        Request request = new Request();
        Map<String, Object> telemetryInfo = new HashMap<>();
        telemetryInfo.put(JsonKey.TELEMETRY_EVENT_TYPE, JsonKey.ERROR);
        telemetryInfo.put(org.sunbird.keys.JsonKey.CONTEXT,requestContext);
        Map<String, Object> params = new HashMap<>();
        params.put(JsonKey.ERR, baseException.getCode());
        params.put(JsonKey.ERR_TYPE,JsonKey.SYSTEM);
        params.put(JsonKey.STACKTRACE, generateStackTrace(e.getStackTrace()));
        if(requestContext!=null) params.put(JsonKey.REQUEST_ID,requestContext.get(JsonKey.REQUEST_ID));
        telemetryInfo.put(org.sunbird.keys.JsonKey.PARAMS,params);
        request.setRequest(telemetryInfo);
        TelemetryWriter.write(request);
    }

    private String generateStackTrace(StackTraceElement[] elements) {
        StringBuilder builder = new StringBuilder("");
        for (StackTraceElement element : elements) {
            builder.append(element.toString());
        }
        return builder.toString();
    }

    private String jsonMapper(RequestContext requestContext, String message, Map<String, Object> object, Map<String, Object> param) {
        try {
            return mapper.writeValueAsString(new CustomLogFormat(requestContext, message, object, param).getEventMap());
        } catch (JsonProcessingException e) {
            error(requestContext, e.getMessage(), e);
        }
        return "";
    }

    // Methods from sb-utils LoggerUtil that are unique (not conflicting with Object adapter)

    public void info(String message) {
        logger.info(message);
    }
    
    public void debug(String message) {
        logger.debug(message);
    }

    // error(Map, String) was unique
    public void error(Map<String,Object> requestContext, String message) {
        logger.error(Markers.appendEntries(requestContext), message);
    }
    // Also add error(Object, String) just in case?
    // If I add error(Object, String), I can merge error(Map, String) into it.
    // PlatformCommon didn't have error(RC, String).
    public void error(Object requestContext, String message) {
         if (requestContext instanceof Map) {
             logger.error(Markers.appendEntries((Map)requestContext), message);
         } else {
             defaultLogger.error(message);
         }
    }

    public void error(String message, Throwable e) {
        logger.error(message, e);
    }

}
