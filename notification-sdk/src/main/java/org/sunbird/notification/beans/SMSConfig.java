/** */
package org.sunbird.notification.beans;

/**
 * This class will keep auth key , sender details for sms.
 *
 * @author manzarul
 */
public class SMSConfig {
  private String authKey;
  private String sender;

  public SMSConfig() {}

  /**
   * @param authKey account key to send sms
   * @param sender sander name , it must be 6 character only.
   */
  public SMSConfig(String authKey, String sender) {
    this.authKey = authKey;
    this.sender = sender;
  }

  public String getAuthKey() {
    return authKey;
  }

  public void setAuthKey(String authKey) {
    this.authKey = authKey;
  }

  public String getSender() {
    return sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }
}
