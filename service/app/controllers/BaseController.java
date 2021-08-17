package controllers;

import akka.actor.ActorRef;
import org.apache.commons.collections.CollectionUtils;
import org.sunbird.Application;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.Localizer;
import org.sunbird.common.request.Request;
import org.sunbird.request.LoggerUtil;
import org.sunbird.common.response.Response;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import utils.RequestMapper;
import validators.RequestValidatorFunction;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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
  private static LoggerUtil logger = new LoggerUtil(BaseController.class);
  public static final String NOTIFICATION_DELIVERY_MODE = "notification-delivery-mode";
  /** We injected HttpExecutionContext to decrease the response time of APIs. */
  @Inject public HttpExecutionContext httpExecutionContext;

  protected static Localizer locale = Localizer.getInstance();
  public static final String RESPONSE = "Response";
  public static final String SUCCESS = "Success";

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
          Request request , RequestValidatorFunction validatorFunction, String operation, play.mvc.Http.Request req) {

    try {
      if (validatorFunction != null) {
        validatorFunction.apply(request);
      }
      List<String> list = req.getHeaders().toMap().get(NOTIFICATION_DELIVERY_MODE);
      if (CollectionUtils.isNotEmpty(list)) {
        request.setManagerName(list.get(0));
      }
      return new ResponseHandler().handleRequest(request, httpExecutionContext, operation, req);
    } catch (BaseException ex) {
      return CompletableFuture.completedFuture(ResponseHandler.handleFailureResponse(request, ex, httpExecutionContext, req));
    } catch (Exception ex) {
      return CompletableFuture.completedFuture( ResponseHandler.handleFailureResponse(request,ex, httpExecutionContext, req));
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
      return CompletableFuture.completedFuture(
      ResponseHandler.handleFailureResponse(request, ex, httpExecutionContext, null ));
    }
    return CompletableFuture.completedFuture(
            ResponseHandler.handleSuccessResponse(request, response, httpExecutionContext, null));
  }
}
