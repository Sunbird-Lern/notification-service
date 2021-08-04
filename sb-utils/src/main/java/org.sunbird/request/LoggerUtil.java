package org.sunbird.request;

import net.logstash.logback.marker.Markers;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class LoggerUtil {

  private Logger logger;

  public LoggerUtil(Class c) {
    logger = LoggerFactory.getLogger(c);
  }

  public void info(RequestContext requestContext, String message) {
    if (null != requestContext) {
      logger.info(Markers.appendEntries(requestContext.getContextMap()), message);
    } else {
      logger.info(message);
    }
  }

  public void info(String message) {
    logger.info(message);
  }

  public void error(RequestContext requestContext, String message, Throwable e) {
    if (null != requestContext) {
      logger.error(Markers.appendEntries(requestContext.getContextMap()), message, e);
    } else {
      logger.error(message, e);
    }
  }

  public void error(String message, Throwable e) {
    logger.error(message, e);
  }

  public void error(
      RequestContext requestContext,
      String message,
      Throwable e,
      Map<String, Object> telemetryInfo) {
    if (null != requestContext) {
      logger.error(Markers.appendEntries(requestContext.getContextMap()), message, e);
    } else {
      logger.error(message, e);
    }
  }

  public void warn(RequestContext requestContext, String message, Throwable e) {
    if (null != requestContext) {
      logger.warn(Markers.appendEntries(requestContext.getContextMap()), message, e);
    } else {
      logger.warn(message, e);
    }
  }

  public void debug(RequestContext requestContext, String message) {
    if (isDebugEnabled(requestContext)) {
      logger.info(Markers.appendEntries(requestContext.getContextMap()), message);
    } else {
      logger.debug(message);
    }
  }

  public void debug(String message) {
    logger.debug(message);
  }

  private static boolean isDebugEnabled(RequestContext requestContext) {
    return (null != requestContext
        && StringUtils.equalsIgnoreCase("true", requestContext.getDebugEnabled()));
  }
}
