/** */
package org.sunbird.notification.beans;

import java.io.Serializable;

/** @author manzarul */
public class MessageResponse implements Serializable {
  /** */
  private static final long serialVersionUID = -5863929694346592115L;

  private String message;
  private String type;
  private String code;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }
}
