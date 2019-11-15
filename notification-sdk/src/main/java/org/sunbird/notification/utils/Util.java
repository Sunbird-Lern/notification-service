/** */
package org.sunbird.notification.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** @author manzarul */
public class Util {
  private static Logger logger = LogManager.getLogger(Util.class);
  private static PropertiesCache propertiesCache = PropertiesCache.getInstance();

  /**
   * This method will check key ,if key is null or empty then it will return null value, if key is
   * not null/empty then first it will fetch value from Environment , if value not set in ENV then
   * it will read from properties file.
   *
   * @param key
   * @return
   */
  public static String readValue(String key) {
    if (StringUtils.isBlank(key)) {
      logger.info("Provided key is either null or emapty :" + key);
      return null;
    }
    String val = System.getenv(key);
    if (StringUtils.isBlank(val)) {
      val = propertiesCache.getProperty(key);
    }
    logger.info("found value for key:" + key + " value: " + val);
    return val;
  }
}
