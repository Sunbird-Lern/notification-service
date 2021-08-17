package org.sunbird.common.exception;

public class BaseException extends RuntimeException {

  private static final long serialVersionUID = 1L;
  private String code;
  private String message;
  private int responseCode;

  /**
   * This code is for client to identify the error and based on that do the message localization.
   *
   * @return String
   */
  public String getCode() {
    return code;
  }

  /**
   * To set the client code.
   *
   * @param code String
   */
  public void setCode(String code) {
    this.code = code;
  }

  /**
   * message for client in english.
   *
   * @return String
   */
  @Override
  public String getMessage() {
    return message;
  }

  /** @param message String */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * This method will provide response code, this code will be used in response header.
   *
   * @return int
   */
  public int getResponseCode() {
    return responseCode;
  }

  /** @param responseCode int */
  public void setResponseCode(int responseCode) {
    this.responseCode = responseCode;
  }

  /**
   * three argument constructor.
   *
   * @param code String
   * @param message String
   * @param responseCode int
   */
  public BaseException(String code, String message, int responseCode) {
    super();
    this.code = code;
    this.message = message;
    this.responseCode = responseCode;
  }

  public BaseException(BaseException ex){
    super();
    this.code=ex.code;
    this.message=ex.getMessage();
    this.responseCode=ex.getResponseCode();
  }
  public BaseException(String code, String message) {
    super();
    this.code = code;
    this.message = message;
  }
}
