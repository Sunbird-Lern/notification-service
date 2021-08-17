package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.JsonKey;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.request.Request;
import org.sunbird.request.EntryExitLogEvent;
import org.sunbird.request.LoggerUtil;
import org.sunbird.common.response.Response;
import org.sunbird.common.response.ResponseParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PrintEntryExitLog {

  private static LoggerUtil logger = new LoggerUtil(PrintEntryExitLog.class);
  private static ObjectMapper objectMapper = new ObjectMapper();

  public static void printEntryLog(Request request) {
    try {
      EntryExitLogEvent entryLogEvent = getLogEvent(request, "ENTRY");
      List<Map<String, Object>> params = new ArrayList<>();
      params.add(request.getRequest());
      entryLogEvent.setEdataParams(params);
      logger.info(request.getContext(), entryLogEvent.toString());
    } catch (Exception ex) {
      logger.error("Exception occurred while logging entry log", ex);
    }
  }

  public static void printExitLogOnSuccessResponse(
      Request request, Response response) {
    try {
      EntryExitLogEvent exitLogEvent = getLogEvent(request, "EXIT");
      List<Map<String, Object>> params = new ArrayList<>();
      if (null != response) {
        if (MapUtils.isNotEmpty(response.getResult())) {
          params.add(response.getResult());
        }

        if (null != response.getParams()) {
          Map<String, Object> resParam = new HashMap<>();
          resParam.putAll(objectMapper.convertValue(response.getParams(), Map.class));
          resParam.put(JsonKey.RESPONSE_CODE, response.getResponseCode());
          params.add(resParam);
        }
      }
      exitLogEvent.setEdataParams(params);
      logger.info(request.getContext(), exitLogEvent.toString());
    } catch (Exception ex) {
      logger.error("Exception occurred while logging exit log", ex);
    }
  }

  public static void printExitLogOnFailure(
      Request request, BaseException exception) {
    try {
      EntryExitLogEvent exitLogEvent = getLogEvent(request, "EXIT");
      String requestId = (String) request.getContext().get(JsonKey.REQUEST_ID);
      List<Map<String, Object>> params = new ArrayList<>();
      if (null == exception) {
        exception =
            new BaseException(
                ResponseCode.SERVER_ERROR.name(),
                ResponseCode.SERVER_ERROR.name(),
                ResponseCode.SERVER_ERROR.getCode());
      }

      ResponseCode code = ResponseCode.getResponseCode(exception.getResponseCode());
      if (code == null) {
        code = ResponseCode.SERVER_ERROR;
      }
      ResponseParams responseParams =
          createResponseParamObj(code, exception.getMessage(), requestId);
      if (responseParams != null) {
        responseParams.setStatus(JsonKey.FAILED);
        if (exception.getCode() != null) {
          responseParams.setStatus(JsonKey.FAILED);
        }
        if (!StringUtils.isBlank(responseParams.getErrmsg())
            && responseParams.getErrmsg().contains("{0}")) {
          responseParams.setErrmsg(exception.getMessage());
        }
      }
      if (null != responseParams) {
        Map<String, Object> resParam = new HashMap<>();
        resParam.putAll(objectMapper.convertValue(responseParams, Map.class));
        resParam.put(JsonKey.RESPONSE_CODE, exception.getResponseCode());
        params.add(resParam);
      }
      exitLogEvent.setEdataParams(params);
      logger.info(request.getContext(), exitLogEvent.toString());
    } catch (Exception ex) {
      logger.error("Exception occurred while logging exit log", ex);
    }
  }

  private static EntryExitLogEvent getLogEvent(Request request, String logType) {
    EntryExitLogEvent entryLogEvent = new EntryExitLogEvent();
    entryLogEvent.setEid("LOG");
    String url = (String) request.getContext().get(JsonKey.URL);
    String entryLogMsg =
        logType
            + " LOG: method : "
            + request.getContext().get(JsonKey.METHOD)
            + ", url: "
            + url
            + " , For Operation : "
            + request.getOperation();
    String requestId = (String) request.getContext().get(JsonKey.REQUEST_ID);
    entryLogEvent.setEdata("system", "trace", requestId, entryLogMsg, null);
    return entryLogEvent;
  }

  public static ResponseParams createResponseParamObj(
    ResponseCode code, String customMessage, String requestId) {
    ResponseParams params = new ResponseParams();
    if (code.getCode() != 200) {
      params.setErr(code.name());
      params.setErrmsg(
        StringUtils.isNotBlank(customMessage) ? customMessage : code.name());
    }
    params.setStatus(ResponseCode.getResponseCode(code.getCode()).name());
    params.setMsgid(requestId);
    return params;
  }
}
