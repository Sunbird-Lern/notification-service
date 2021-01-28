package controllers.health;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.BaseController;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import controllers.RequestHandler;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sunbird.BaseException;
import org.sunbird.message.IResponseMessage;
import org.sunbird.message.ResponseCode;
import org.sunbird.response.Response;
import play.libs.Json;
import play.mvc.Result;
import utils.module.SignalHandler;

import javax.inject.Inject;

/**
 * This controller class will responsible to check health of the services.
 *
 * @author Anmol
 */
public class HealthController extends BaseController {
  Logger logger = LogManager.getLogger(HealthController.class);
  @Inject
  SignalHandler signalHandler;
  // Service name must be "service" for the devops monitoring.
  private static final String service = "service";
  private static final String HEALTH_ACTOR_OPERATION_NAME = "health";

  /**
   * This action method is responsible for checking complete service and dependency Health.
   *
   * @return a CompletableFuture of success response
   */
  public CompletionStage<Result> getHealth() {
    try {
      handleSigTerm();
      logger.info("complete health method called.");
      CompletionStage<Result> response = handleRequest(request(), null, HEALTH_ACTOR_OPERATION_NAME);
      return response;
    }  catch (Exception e) {
      return CompletableFuture.completedFuture(RequestHandler.handleFailureResponse(e,httpExecutionContext,request()));
    }
  }

  /**
   * This action method is responsible to check certs-service health
   *
   * @return a CompletableFuture of success response
   */
  public CompletionStage<Result> getServiceHealth(String health) {

    try {
      handleSigTerm();
      logger.info("get healh called for service =." + health);
      CompletableFuture<JsonNode> cf = new CompletableFuture<>();
      Response response = new Response();
      response.put(RESPONSE, SUCCESS);
      cf.complete(Json.toJson(response));
      return CompletableFuture.completedFuture(ok(play.libs.Json.toJson(response)));
      }  catch (Exception e) {
        return CompletableFuture.completedFuture(RequestHandler.handleFailureResponse(e,httpExecutionContext,request()));
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
