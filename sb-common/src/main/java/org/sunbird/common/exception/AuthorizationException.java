package org.sunbird.common.exception;

import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.ResponseCode;

public class AuthorizationException {

  public static class NotAuthorized extends BaseException {
    public NotAuthorized(ResponseCode responseCode) {
      super(responseCode.getErrorCode(), responseCode.getErrorMessage(), 401);
    }
  }
}
