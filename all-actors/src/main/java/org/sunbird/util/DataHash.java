/** */
package org.sunbird.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.sunbird.request.LoggerUtil;

/** @author manzarul */
public class DataHash {
  private static LoggerUtil logger = new LoggerUtil(DataHash.class);

  private DataHash() {}

  /**
   * This method will hash value using SHA-256 . it is one way hashed.
   *
   * @param val String
   * @return String hashed value or empty in case of exception
   */
  public static String getHashed(String val) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update(val.getBytes(StandardCharsets.UTF_8));
      byte byteData[] = md.digest();
      // convert the byte to hex format method 1
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < byteData.length; i++) {
        sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
      }
      logger.info("encrypted value is==: " + sb.toString());
      return sb.toString();
    } catch (Exception e) {
      logger.error("Error while encrypting", e);
    }
    return "";
  }
}
