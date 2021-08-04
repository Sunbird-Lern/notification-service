package controllers;

import akka.pattern.Patterns;
import akka.util.Timeout;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.BaseException;
import org.sunbird.message.IResponseMessage;
import org.sunbird.message.ResponseCode;
import org.sunbird.request.HeaderParam;
import org.sunbird.request.LoggerUtil;
import org.sunbird.request.Request;
import org.sunbird.request.RequestContext;
import org.sunbird.response.Response;
import org.sunbird.response.ResponseParams;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import play.mvc.Results;
import scala.compat.java8.FutureConverters;
import scala.concurrent.Future;
import utils.JsonKey;

import static utils.PrintEntryExitLog.printEntryLog;
import static utils.PrintEntryExitLog.printExitLogOnFailure;
import static utils.PrintEntryExitLog.printExitLogOnSuccessResponse;

/**
 * this class is used to handle the request and ask from actor and return response on the basis of
 * success and failure to user.
 *
 * @author amitkumar
 */
public class RequestHandler extends BaseController {
  
  private static LoggerUtil logger = new LoggerUtil(RequestHandler.class);
  
  /**
   * this methis responsible to handle the request and ask from actor
   *
   * @param request
   * @param httpExecutionContext
   * @param operation
   * @return CompletionStage<Result>
   * @throws Exception
   */
  public CompletionStage<Result> handleRequest(
    Request request,
    HttpExecutionContext httpExecutionContext,
    String operation,
    play.mvc.Http.Request req)
    throws Exception {
    request.setOperation(operation);
    setContextData(req, request);
    printEntryLog(request);
    Function<Object, Result> fn =
      object -> handleResponse(object, httpExecutionContext, req, request);
    
    Timeout t = new Timeout(Long.valueOf(request.getTimeout()), TimeUnit.SECONDS);
    Future<Object> future = Patterns.ask(getActorRef(operation), request, t);
    return FutureConverters.toJava(future).thenApplyAsync(fn);
  }
  
  /**
   * This method will handle all the failure response of Api calls.
   *
   * @param exception
   * @return
   */
  public static Result handleFailureResponse(
    Object exception, HttpExecutionContext httpExecutionContext ,play.mvc.Http.Request req, Request request) {
    
    Response response = new Response();
    response.getParams().setStatus(JsonKey.FAILED);
    response.getParams().setMsgid(request.getRequestId());
    CompletableFuture<JsonNode> future = new CompletableFuture<>();
    if (exception instanceof BaseException) {
      BaseException ex = (BaseException) exception;
      response.setResponseCode(ResponseCode.getResponseCode(ex.getResponseCode()));
      response.put(JsonKey.MESSAGE, ex.getMessage());
      String apiId = getApiId(req.path());
      response.setId(apiId);
      response.setVer("v1");
      response.setTs(System.currentTimeMillis() + "");
      future.complete(Json.toJson(response));
      printExitLogOnFailure(request,ex);
      if (ex.getResponseCode() == Results.badRequest().status()) {
        return  Results.badRequest(Json.toJson(response));
      } else if (ex.getResponseCode() == 503) {
        return Results.status(
          ex.getResponseCode(),
          Json.toJson(createResponseOnException(ex)));
      } else {
        return Results.internalServerError();
      }
    } else {
      response.setResponseCode(ResponseCode.SERVER_ERROR);
      response.put(
        JsonKey.MESSAGE, locale.getMessage(IResponseMessage.INTERNAL_ERROR, null));
      future.complete(Json.toJson(response));
      printExitLogOnFailure(request, null);
      return Results.internalServerError(Json.toJson(response));
    }
  }
  
  /**
   * This method will handle all the failure response of Api calls.
   *
   * @param exception
   * @return
   */
  public static Result handleFailureResponse(
    Object exception, HttpExecutionContext httpExecutionContext ,play.mvc.Http.Request req) {
    
    Response response = new Response();
    CompletableFuture<JsonNode> future = new CompletableFuture<>();
    if (exception instanceof BaseException) {
      BaseException ex = (BaseException) exception;
      response.setResponseCode(ResponseCode.getResponseCode(ex.getResponseCode()));
      response.put(JsonKey.MESSAGE, ex.getMessage());
      String apiId = getApiId(req.path());
      response.setId(apiId);
      response.setVer("v1");
      response.setTs(System.currentTimeMillis() + "");
      future.complete(Json.toJson(response));
      if (ex.getResponseCode() == Results.badRequest().status()) {
        return  Results.badRequest(Json.toJson(response));
      } else if (ex.getResponseCode() == 503) {
        return Results.status(
          ex.getResponseCode(),
          Json.toJson(createResponseOnException(ex)));
      } else {
        return Results.internalServerError();
      }
    } else {
      response.setResponseCode(ResponseCode.SERVER_ERROR);
      response.put(
        JsonKey.MESSAGE, locale.getMessage(IResponseMessage.INTERNAL_ERROR, null));
      future.complete(Json.toJson(response));
      return Results.internalServerError(Json.toJson(response));
    }
  }
  
  
  public static Response createResponseOnException(BaseException exception) {
    Response response = new Response();
    response.setResponseCode(ResponseCode.getResponseCode(exception.getResponseCode()));
    response.setParams(createResponseParamObj(response.getResponseCode(), exception.getMessage()));
    return response;
  }
  
  public static ResponseParams createResponseParamObj(ResponseCode code, String message) {
    ResponseParams params = new ResponseParams();
    if (code.getCode() != 200) {
      params.setErr(code.name());
      params.setErrmsg(StringUtils.isNotBlank(message) ? message : code.name());
    }
    params.setStatus(ResponseCode.getResponseCode(code.getCode()).name());
    return params;
  }
  
  /**
   * this method will divert the response on the basis of success and failure
   *
   * @param object
   * @param httpExecutionContext
   * @return
   */
  public static Result handleResponse(
    Object object, HttpExecutionContext httpExecutionContext, play.mvc.Http.Request req, Request request) {
    
    if (object instanceof Response) {
      Response response = (Response) object;
      response.setParams(createResponseParamObj(response.getResponseCode(), null));
      response.getParams().setMsgid(request.getRequestContext().getReqId());
      printExitLogOnSuccessResponse(request, response);
      return handleSuccessResponse(response, httpExecutionContext, req);
    } else {
      return handleFailureResponse(object, httpExecutionContext, req, request);
    }
  }
  
  /**
   * This method will handle all the success response of Api calls.
   *
   * @param response
   * @return
   */
  public static Result handleSuccessResponse(
    Response response, HttpExecutionContext httpExecutionContext, play.mvc.Http.Request req) {
    CompletableFuture<JsonNode> future = new CompletableFuture<>();
    String apiId = getApiId(req.path());
    response.setId(apiId);
    response.setVer("v1");
    response.setTs(System.currentTimeMillis() + "");
    response.getParams().setStatus(JsonKey.SUCCESS);
    future.complete(Json.toJson(response));
    return Results.ok(Json.toJson(response));
  }
  
  public static String getApiId(String uri) {
    StringBuilder builder = new StringBuilder();
    if (StringUtils.isNotBlank(uri)) {
      String temVal[] = uri.split("/");
      for (int i = 1; i < temVal.length; i++) {
        if (i < temVal.length - 1) {
          builder.append(temVal[i] + ".");
        } else {
          builder.append(temVal[i]);
        }
      }
    }
    return builder.toString();
  }
  
  
  public void setContextData(play.mvc.Http.Request httpReq, Request request) {
    try {
      Map<String, Object> reqContext = new WeakHashMap<>();
      Optional<String> optionalAppId = httpReq.header(HeaderParam.X_APP_ID.getName());
      if (optionalAppId.isPresent()) {
        reqContext.put(org.sunbird.JsonKey.APP_ID, optionalAppId.get());
      }
      Optional<String> optionalDeviceId = httpReq.header(HeaderParam.X_Device_ID.getName());
      if (optionalDeviceId.isPresent()) {
        reqContext.put(org.sunbird.JsonKey.DEVICE_ID, optionalDeviceId.get());
      }
      
      Optional<String> optionalSessionId = httpReq.header(HeaderParam.X_Session_ID.getName());
      if (optionalSessionId.isPresent()) {
        reqContext.put(org.sunbird.JsonKey.X_Session_ID, optionalSessionId.get());
      }
      
      Optional<String> optionalAppVersion = httpReq.header(HeaderParam.X_APP_VERSION.getName());
      if (optionalAppVersion.isPresent()) {
        reqContext.put(org.sunbird.JsonKey.X_APP_VERSION, optionalAppVersion.get());
      }
      
      Optional<String> optionalTraceEnabled = httpReq.header(HeaderParam.X_TRACE_ENABLED.getName());
      if (optionalTraceEnabled.isPresent()) {
        reqContext.put(org.sunbird.JsonKey.X_TRACE_ENABLED, optionalTraceEnabled.get());
      }
      
      Optional<String> optionalTraceId = httpReq.header(HeaderParam.X_REQUEST_ID.getName());
      if (optionalTraceId.isPresent()) {
        String reqId = optionalTraceId.get();
        reqContext.put(org.sunbird.JsonKey.X_REQUEST_ID, reqId);
        request.setRequestId(optionalTraceId.get());
      } else {
        String reqId = UUID.randomUUID().toString();
        reqContext.put(org.sunbird.JsonKey.X_REQUEST_ID, reqId);
        request.setRequestId(reqId);
      }
      request.getContext().put(JsonKey.URL,httpReq.uri());
      request.getContext().put(JsonKey.METHOD,httpReq.method());
      request.setRequestContext(
        getRequestContext(
          reqContext, request.getOperation()));
    } catch (Exception ex) {
      logger.error("Exception occurred while setting request context.", ex);
    }
  }
  
  private RequestContext getRequestContext(Map<String, Object> context, String actorOperation) {
    return new RequestContext(
      (String) context.get(org.sunbird.JsonKey.ACTOR_ID),
      (String) context.get(org.sunbird.JsonKey.DEVICE_ID),
      (String) context.get(org.sunbird.JsonKey.X_Session_ID),
      (String) context.get(org.sunbird.JsonKey.APP_ID),
      (String) context.get(org.sunbird.JsonKey.X_APP_VERSION),
      (String) context.get(org.sunbird.JsonKey.X_REQUEST_ID),
      (String)
        ((context.get(org.sunbird.JsonKey.X_TRACE_ENABLED) != null)
          ? context.get(org.sunbird.JsonKey.X_TRACE_ENABLED)
          : "false"),
      actorOperation);
  }
}