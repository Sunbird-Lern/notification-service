package org.sunbird.common.exception;

public class ActorServiceException {

  public static class InvalidOperationName extends BaseException
  {
    public InvalidOperationName(String code, String message, int responseCode) {
      super(code,message,responseCode);
    }
  }

  public static class InvalidRequestTimeout extends BaseException
  {
    public InvalidRequestTimeout(String code, String message, int responseCode) {
      super(code,message,responseCode);
    }
  }

  public static class InvalidRequestData extends BaseException
  {
    public InvalidRequestData(String code, String message, int responseCode) {
      super(code,message,responseCode);
    }
  }

}