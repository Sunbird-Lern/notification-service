package controllers.health;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.BaseController;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import controllers.ResponseHandler;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.request.Request;
import org.sunbird.request.LoggerUtil;
import org.sunbird.common.response.Response;
import play.libs.Json;
import play.mvc.Result;
import utils.RequestMapper;

import javax.inject.Inject;

/**
 * This controller class will responsible to check health of the services.
 *
 * @author Anmol
 */
public class HealthController extends BaseController {
  private static LoggerUtil logger = new LoggerUtil(HealthController.class);
  @Inject
  utils.module.SignalHandler signalHandler;
  // Service name must be "service" for the devops monitoring.
  private static final String service = "service";
  private static final String HEALTH_ACTOR_OPERATION_NAME = "health";

  /**
   * This action method is responsible for checking complete service and dependency Health.
   *
   * @return a CompletableFuture of success response
   */
  public CompletionStage<Result> getHealth() {
    Request request = new Request();
    try {
      handleSigTerm();
      logger.info("complete health method called.");
      request = RequestMapper.createSBRequest(request());
      CompletionStage<Result> response = handleRequest(request, null, HEALTH_ACTOR_OPERATION_NAME, request());
      return response;
    }  catch (Exception e) {
      return CompletableFuture.completedFuture(ResponseHandler.handleFailureResponse(request,e ,httpExecutionContext,request()));
    }
  }

  /**
   * This action method is responsible to check certs-service health
   *
   * @return a CompletableFuture of success response
   */
  public CompletionStage<Result> getServiceHealth(String health) {
    Request request = new Request();
    try {
      handleSigTerm();
      logger.info("get healh called for service =." + health);
      request = RequestMapper.createSBRequest(request());
      CompletableFuture<JsonNode> cf = new CompletableFuture<>();
      Response response = new Response();
      response.put(RESPONSE, SUCCESS);
      cf.complete(Json.toJson(response));
      return CompletableFuture.completedFuture(ok(play.libs.Json.toJson(response)));
      }  catch (Exception e) {
        return CompletableFuture.completedFuture(ResponseHandler.handleFailureResponse(request,e,httpExecutionContext,request()));
      }
  }

  private void handleSigTerm() throws BaseException {
    if (signalHandler.isShuttingDown()) {
      logger.info(
              "SIGTERM is "
                      + signalHandler.isShuttingDown()
                      + ", So play server will not allow any new request.");
      throw new BaseException(IResponseMessage.SERVICE_UNAVAILABLE, IResponseMessage.SERVICE_UNAVAILABLE, ResponseCode.SERVICE_UNAVAILABLE.getCode());
    }
  }
}
