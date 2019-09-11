package controllers.health;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.BaseController;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sunbird.response.Response;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;

/**
 * This controller class will responsible to check health of the services.
 *
 * @author Anmol
 */
public class HealthController extends BaseController {
  Logger logger = LogManager.getLogger(HealthController.class);
  // Service name must be "service" for the devops monitoring.
  private static final String service = "service";
  private static final String HEALTH_ACTOR_OPERATION_NAME = "health";

  /**
   * This action method is responsible for checking complete service and dependency Health.
   *
   * @return a CompletableFuture of success response
   */
  public CompletionStage<Result> getHealth() {
    logger.info("complete health method called.");
    CompletionStage<Result> response = handleRequest(request(), null, HEALTH_ACTOR_OPERATION_NAME);
    return response;
  }

  /**
   * This action method is responsible to check certs-service health
   *
   * @return a CompletableFuture of success response
   */
  public CompletionStage<Result> getServiceHealth(String health) {
    logger.info("get healh called for service =." + health);
    CompletableFuture<JsonNode> cf = new CompletableFuture<>();
    Response response = new Response();
    response.put(RESPONSE, SUCCESS);
    cf.complete(Json.toJson(response));
    return service.equalsIgnoreCase(health)
        ? cf.thenApplyAsync(Results::ok)
        : cf.thenApplyAsync(Results::badRequest);
  }
}
