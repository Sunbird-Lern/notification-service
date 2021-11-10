package org.sunbird.common.response;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.request.Request;
import org.sunbird.common.message.Localizer;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.util.JsonKey;

public class ResponseFactory {
  private static final String msgKeyStr = "message";
  protected static Localizer localizerObject = Localizer.getInstance();

  /**
   * this method will prepare the failure response of the API
   *
   * @param exception
   * @return
   */
  public static Response getFailureMessage(Object exception, Request request) {
   Response response = new Response();
    if (request != null) {
      response.setId(getApiId(request.getPath()));
      response.setVer(request.getVer());
      response.setTs(getCurrentDate());
    }
    if (exception instanceof BaseException) {
      BaseException ex = (BaseException) exception;
      ResponseCode code = ResponseCode.getResponse(ex.getCode());
      if (code == null) {
        code = ResponseCode.SERVER_ERROR;
      }
      response.setParams(createResponseParamObj(request, code, ex.getMessage()));
      if (response.getParams() != null) {
        if (!StringUtils.isBlank(response.getParams().getErrmsg())
                && response.getParams().getErrmsg().contains("{0}")) {
          response.getParams().setErrmsg(ex.getMessage());
        }
      }
      response.setResponseCode(ResponseCode.getResponseCode(ex.getResponseCode()));
    }
    return response;
  }

  public static ResponseParams createResponseParamObj(
          Request request, ResponseCode code, String customMessage) {
    ResponseParams params = new ResponseParams();
    if (code.getCode() != 200) {
      params.setErr(code.getErrorCode());
      params.setErrmsg(StringUtils.isNotBlank(customMessage) ? customMessage : code.name());
    }
    setResponseParams(request, params);
    return params;
  }

  private static void setResponseParams(Request request, ResponseParams params) {
    if (request.getContext().containsKey(JsonKey.X_REQUEST_ID)) {
      String msgId = (String) request.getContext().get(JsonKey.X_REQUEST_ID);
      params.setMsgid(msgId);
    }
  }

  public static Response getSuccessMessage(Request request) {
   Response response = new Response();
    response.setId(request.getId());
    response.setVer(request.getVer());
    response.setTs(System.currentTimeMillis() + StringUtils.EMPTY);
    ResponseParams params = new ResponseParams();
    setResponseParams(request, params);
    return response;
  }

  public static String getApiId(String uri) {
    final String ver = "/" + JsonKey.API_VERSION;
    StringBuilder builder = new StringBuilder();
    if (StringUtils.isNotBlank(uri)) {
      if (uri.contains(ver)) {
        uri = uri.replaceFirst(ver, "api");
      }
      String temVal[] = uri.split("/");
      for (String str : temVal) {
        if (str.matches("[A-Za-z]+")) {
          builder.append(str + ".");
        }
      }
      builder.deleteCharAt(builder.length() - 1);
    }
    return builder.toString();
  }

  public static String getCurrentDate() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSSZ");
    simpleDateFormat.setLenient(false);
    return simpleDateFormat.format(new Date());
  }
}
