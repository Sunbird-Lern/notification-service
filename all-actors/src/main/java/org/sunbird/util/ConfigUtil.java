package org.sunbird.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.request.LoggerUtil;

/**
 * This util class for providing type safe config to any service that requires it.
 *
 * @author Manzarul
 */
public class ConfigUtil {

  private static LoggerUtil logger = new LoggerUtil(ConfigUtil.class);
  private static Config config;
  private static final String DEFAULT_TYPE_SAFE_CONFIG_FILE_NAME = "service.conf";
  private static final String INVALID_FILE_NAME = "Please provide a valid file name.";

  /** Private default constructor. */
  private ConfigUtil() {}

  /**
   * This method will create a type safe config object and return to caller. It will read the config
   * value from System env first and as a fall back it will use service.conf file.
   *
   * @return Type safe config object
   */
  public static Config getConfig() {
    if (config == null) {
      synchronized (ConfigUtil.class) {
        config = createConfig(DEFAULT_TYPE_SAFE_CONFIG_FILE_NAME);
      }
    }
    return config;
  }

  /**
   * This method will create a type safe config object and return to caller. It will read the config
   * value from System env first and as a fall back it will use provided file name. If file name is
   * null or empty then it will throw ProjectCommonException with status code as 500.
   *
   * @return Type safe config object
   */
  public static Config getConfig(String fileName) {
    if (StringUtils.isBlank(fileName)) {
      logger.info("ConfigUtil:getConfigWithFilename: Given file name is null or empty: ");
      throw new RuntimeException(INVALID_FILE_NAME);
    }
    if (config == null) {
      synchronized (ConfigUtil.class) {
        config = createConfig(fileName);
      }
    }
    return config;
  }

  public static void validateMandatoryConfigValue(String configParameter) {
    if (StringUtils.isBlank(configParameter)) {
      logger.info(
          "ConfigUtil:validateMandatoryConfigValue: Missing mandatory configuration parameter: "
              + configParameter);
      throw new RuntimeException("Server Error");
    }
  }

  private static Config createConfig(String fileName) {
    Config defaultConf = ConfigFactory.load(fileName);
    Config envConf = ConfigFactory.systemEnvironment();
    return envConf.withFallback(defaultConf);
  }

  /*
   * Parse configuration in JSON format and return a type safe config object.
   *
   * @param jsonString Configuration in JSON format
   * @return Type safe config object
   */
  public static Config getConfigFromJsonString(String jsonString, String configType) {
    logger.info("ConfigUtil: getConfigFromJsonString called");

    if (null == jsonString || StringUtils.isBlank(jsonString)) {
      logger.info("ConfigUtil:getConfigFromJsonString: Empty string");
      return null;
    }

    Config jsonConfig = null;
    try {
      jsonConfig = ConfigFactory.parseString(jsonString);
    } catch (Exception e) {
      logger.error(
          "ConfigUtil:getConfigFromJsonString: Exception occurred during parse with error message = "
              + e.getMessage(), e);
      return null;
    }

    if (null == jsonConfig || jsonConfig.isEmpty()) {
      logger.info("ConfigUtil:getConfigFromJsonString: Empty configuration");

      return null;
    }

    logger.info(
        "ConfigUtil:getConfigFromJsonString: Successfully constructed type safe configuration");

    return jsonConfig;
  }
}
