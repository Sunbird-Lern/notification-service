package org.sunbird.message;

/** @author Manzarul */
public enum ResponseCode {
  OK(200),
  CLIENT_ERROR(400),
  SERVER_ERROR(500),
  RESOURCE_NOT_FOUND(404),
  UNAUTHORIZED(401),
  FORBIDDEN(403),
  REDIRECTION_REQUIRED(302),
  TOO_MANY_REQUESTS(429),
  SERVICE_UNAVAILABLE(503),
  BAD_REQUEST(400);

  private int code;

  ResponseCode(int code) {
    this.code = code;
  }

  public int getCode() {
    return this.code;
  }

}
