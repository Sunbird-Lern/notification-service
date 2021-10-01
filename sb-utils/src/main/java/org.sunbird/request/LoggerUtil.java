package org.sunbird.request;

import net.logstash.logback.marker.Markers;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.JsonKey;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.request.Request;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.telemetry.util.TelemetryWriter;

import java.util.HashMap;
import java.util.Map;

public class LoggerUtil {

  private Logger logger;

  public LoggerUtil(Class c) {
    logger = LoggerFactory.getLogger(c);
  }

  public void info(Map<String,Object> requestContext, String message) {
    if (null != requestContext) {
      logger.info(Markers.appendEntries(requestContext), message);
    } else {
      logger.info(message);
    }
  }

  public void info(String message) {
    logger.info(message);
  }

  public void error(Map<String,Object> requestContext, String message, Throwable e) {
    if (null != requestContext) {
      logger.error(Markers.appendEntries(requestContext), message, e);
    } else {
      logger.error(message, e);
    }
  }

  public void error(Map<String,Object> requestContext,String message) {
    logger.error(Markers.appendEntries(requestContext), message);
  }

  public void error(String message, Throwable e) {
    logger.error(message, e);
  }

  public void error(
      Map<String,Object> requestContext,
      String message,
      Throwable e,
      Map<String, Object> telemetryInfo) {
    if (null != requestContext) {
      logger.error(Markers.appendEntries(requestContext), message, e);
    } else {
      logger.error(message, e);
    }
    telemetryProcess(requestContext, e);
  }

  public void warn(Map<String,Object> requestContext, String message, Throwable e) {
    if (null != requestContext) {
      logger.warn(Markers.appendEntries(requestContext), message, e);
    } else {
      logger.warn(message, e);
    }
  }

  public void debug(Map<String,Object> requestContext, String message) {
    if (isDebugEnabled(requestContext)) {
      logger.info(Markers.appendEntries(requestContext), message);
    } else {
      logger.debug(message);
    }
  }

  public void debug(String message) {
    logger.debug(message);
  }

  private static boolean isDebugEnabled(Map<String,Object> requestContext) {
    return (null != requestContext
        && StringUtils.equalsIgnoreCase("true", (String)requestContext.get(JsonKey.DEBUG_ENABLED)));
  }

  private void telemetryProcess(
          Map<String,Object> requestContext, Throwable e) {
    BaseException baseException = null;
    if (e instanceof BaseException) {
      baseException = (BaseException) e;
    } else {
      baseException =
              new BaseException(
                      ResponseCode.SERVER_ERROR.getErrorCode(),
                      ResponseCode.SERVER_ERROR.getErrorMessage(),
                      ResponseCode.SERVER_ERROR.getCode());
    }
    Request request = new Request();
    Map<String, Object> telemetryInfo = new HashMap<>();
    telemetryInfo.put(JsonKey.TELEMETRY_EVENT_TYPE, JsonKey.ERROR);
    telemetryInfo.put(org.sunbird.JsonKey.CONTEXT,requestContext);
    Map<String, Object> params = new HashMap<>();
    params.put(JsonKey.ERR, baseException.getCode());
    params.put(JsonKey.ERR_TYPE,JsonKey.SYSTEM);
    params.put(JsonKey.STACKTRACE, generateStackTrace(e.getStackTrace()));
    params.put(JsonKey.REQUEST_ID,requestContext.get(JsonKey.REQUEST_ID));
    telemetryInfo.put(org.sunbird.JsonKey.PARAMS,params);
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
}
