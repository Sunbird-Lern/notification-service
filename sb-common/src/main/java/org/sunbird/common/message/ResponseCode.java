package org.sunbird.common.message;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.util.JsonKey;
public enum ResponseCode {
  unAuthorized(IResponseMessage.Key.UNAUTHORIZED, IResponseMessage.Message.UNAUTHORIZED, JsonKey.FAILED),
  mandatoryParameterMissing(
          IResponseMessage.Key.MANDATORY_PARAMETER_MISSING, IResponseMessage.Message.MANDATORY_PARAMETER_MISSING,JsonKey.FAILED),
  invalidRequestData(
      IResponseMessage.INVALID_REQUESTED_DATA, IResponseMessage.Message.INVALID_REQUESTED_DATA,JsonKey.FAILED),
  serverError(IResponseMessage.SERVER_ERROR, IResponseMessage.SERVER_ERROR,JsonKey.FAILED),
  internalError(IResponseMessage.INTERNAL_ERROR, IResponseMessage.INTERNAL_ERROR,JsonKey.FAILED),

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
  /** error code contains String value */
  private String errorCode;
  /** errorMessage contains proper error message. */
  private String errorMessage;

  private String status;

  ResponseCode(int code) {
    this.code = code;
  }

  /**
   * @param errorCode String
   * @param errorMessage String
   */
  ResponseCode(String errorCode, String errorMessage, String status) {
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
    this.status = status;
  }
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public int getCode() {
    return this.code;
  }

  /**
   * This method will take header response code as int value and it provide matched enum value, if
   * code is not matched or exception occurs then it will provide SERVER_ERROR
   *
   * @param code int
   * @return HeaderResponseCode
   */
  public static ResponseCode getHeaderResponseCode(int code) {
    if (code > 0) {
      try {
        ResponseCode[] arr = ResponseCode.values();
        if (null != arr) {
          for (ResponseCode rc : arr) {
            if (rc.getResponseCode() == code) return rc;
          }
        }
      } catch (Exception e) {
        return ResponseCode.SERVER_ERROR;
      }
    }
    return ResponseCode.SERVER_ERROR;
  }

  /**
   * This method will take header response error code as string value and it provide matched enum value, if
   * error code is not matched or exception occurs then it will provide SERVER_ERROR
   *
   * @param errorCode String
   * @return HeaderResponseCode
   */
  public static ResponseCode getHeaderResponseStatus(String errorCode) {
    if (errorCode!=  null) {
      try {
        ResponseCode[] arr = ResponseCode.values();
        if (null != arr) {
          for (ResponseCode rc : arr) {
            if (rc.getErrorCode().equals(errorCode)) return rc;
          }
        }
      } catch (Exception e) {
        return ResponseCode.SERVER_ERROR;
      }
    }
    return ResponseCode.SERVER_ERROR;
  }

  /**
   * This method will provide ResponseCode enum based on error code
   *
   * @param errorCode
   * @return String
   */
  public static ResponseCode getResponse(String errorCode) {
    if (StringUtils.isBlank(errorCode)) {
      return null;
    } else if (JsonKey.UNAUTHORIZED.equals(errorCode)) {
      return ResponseCode.unAuthorized;
    } else {
      ResponseCode value = null;
      ResponseCode responseCodes[] = ResponseCode.values();
      for (ResponseCode response : responseCodes) {
        if (response.getErrorCode().equals(errorCode)) {
          return response;
        }
      }
      return value;
    }
  }

  public int getResponseCode() {
    return code;
  }

  /** @return */
  public String getErrorCode() {
    return errorCode;
  }

  /** @return */
  public String getErrorMessage() {
    return errorMessage;
  }

  public static ResponseCode getResponseCode(int code) {
    ResponseCode[] codes = ResponseCode.values();
    for (ResponseCode res : codes) {
      if (res.code == code) {
        return res;
      }
    }
    return ResponseCode.RESOURCE_NOT_FOUND;
  }
}
