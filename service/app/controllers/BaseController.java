package controllers;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sunbird.Application;
import org.sunbird.BaseException;
import org.sunbird.message.Localizer;
import org.sunbird.request.Request;
import org.sunbird.response.Response;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import utils.RequestMapper;
import utils.RequestValidatorFunction;

/**
 * This controller we can use for writing some common method to handel api request.
 * CompletableFuture: A Future that may be explicitly completed (setting its value and status), and
 * may be used as a CompletionStage, supporting dependent functions and actions that trigger upon
 * its completion. CompletionStage: A stage of a possibly asynchronous computation, that performs an
 * action or computes a value when another CompletionStage completes
 *
 * @author Anmol
 */
public class BaseController extends Controller {
  Logger logger = LogManager.getLogger(BaseController.class);
  public static final String NOTIFICATION_DELIVERY_MODE = "notification-delivery-mode";
  /** We injected HttpExecutionContext to decrease the response time of APIs. */
  @Inject public HttpExecutionContext httpExecutionContext;

  protected static Localizer locale = Localizer.getInstance();
  public static final String RESPONSE = "Response";
  public static final String SUCCESS = "Success";

  public CompletionStage<Result> handleRequest() {
    startTrace("handelRequest");
    CompletableFuture<JsonNode> future = new CompletableFuture<>();
    Response response = new Response();
    response.put(RESPONSE, SUCCESS);
    future.complete(Json.toJson(response));
    endTrace("handelRequest");
    return future.thenApplyAsync(Results::ok, httpExecutionContext.current());
  }

  /**
   * This method will return the current timestamp.
   *
   * @return long
   */
  public long getTimeStamp() {
    return System.currentTimeMillis();
  }

  /**
   * This method we used to print the logs of starting time of methods
   *
   * @param tag
   */
  public void startTrace(String tag) {
    logger.info("Method call started.");
  }

  /**
   * This method we used to print the logs of ending time of methods
   *
   * @param tag
   */
  public void endTrace(String tag) {
    logger.info("Method call ended.");
  }

  protected ActorRef getActorRef(String operation) throws BaseException {
    return Application.getInstance().getActorRef(operation);
  }

  /**
   * this method will take play.mv.http request and a validation function and lastly operation(Actor
   * operation) this method is validating the request and , it will map the request to our sunbird
   * Request class and make a call to requestHandler which is internally calling ask to actor this
   * method is used to handle all the request type which has requestBody
   *
   * @param req
   * @param validatorFunction
   * @param operation
   * @return
   */
  public CompletionStage<Result> handleRequest(
      play.mvc.Http.Request req, RequestValidatorFunction validatorFunction, String operation) {
    try {
      Request request = new Request();
      List<String> list = req.getHeaders().toMap().get(NOTIFICATION_DELIVERY_MODE);
      if (req.body() != null && req.body().asJson() != null) {
        request = (Request) RequestMapper.mapRequest(req, Request.class);
      }
      if (validatorFunction != null) {
        validatorFunction.apply(request);
      }
      if (CollectionUtils.isNotEmpty(list)) {
        request.setManagerName(list.get(0));
      }
      return new RequestHandler().handleRequest(request, httpExecutionContext, operation, req);
    } catch (BaseException ex) {
      return (CompletionStage<Result>)
              RequestHandler.handleFailureResponse(ex, httpExecutionContext, req);
    } catch (Exception ex) {
      return (CompletionStage<Result>)
              RequestHandler.handleFailureResponse(ex, httpExecutionContext, req);
    }
  }

  /**
   * this method is used to handle the only GET requests.
   *
   * @param req
   * @param operation
   * @return
   */
  public CompletionStage<Result> handleRequest(
      Request req, String operation, play.mvc.Http.Request httpReq) {
    try {
      return new RequestHandler().handleRequest(req, httpExecutionContext, operation, httpReq);
    } catch (BaseException ex) {
      return (CompletionStage<Result>)
              RequestHandler.handleFailureResponse(ex, httpExecutionContext, httpReq);
    } catch (Exception ex) {
      return (CompletionStage<Result>)
              RequestHandler.handleFailureResponse(ex, httpExecutionContext, httpReq);
    }
  }

  /**
   * This method is used specifically to handel Log Apis request this will set log levels and then
   * return the CompletionStage of Result
   *
   * @return
   */
  public CompletionStage<Result> handleLogRequest() {
    startTrace("handleLogRequest");
    Response response = new Response();
    Request request = null;
    try {
      request = (Request) RequestMapper.mapRequest(request(), Request.class);
    } catch (Exception ex) {
      // ProjectLogger.log(String.format("%s:%s:exception occurred in mapping
      // request", this.getClass().getSimpleName(), "handleLogRequest"),
      // LoggerEnum.ERROR.name());
      return (CompletionStage<Result>)
      RequestHandler.handleFailureResponse(ex, httpExecutionContext, null);
    }
    return (CompletionStage<Result>)
            RequestHandler.handleSuccessResponse(response, httpExecutionContext, null);
  }
}
