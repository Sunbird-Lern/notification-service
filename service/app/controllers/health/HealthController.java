package controllers.health;

import controllers.BaseController;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.sunbird.response.Response;

import com.fasterxml.jackson.databind.JsonNode;

import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;

/**
 * This controller class will responsible to check health of the services.
 *
 * @author Anmol
 */
public class HealthController extends BaseController {
	 // Service name must be "service" for the devops monitoring.
	  private static final String service = "service";
	  private static final String HEALTH_ACTOR_OPERATION_NAME = "health";

	  /**
	   * This action method is responsible for checking complete service and dependency Health.
	   *
	   * @return a CompletableFuture of success response
	   */
	  public CompletionStage<Result> getHealth() {
		  CompletionStage<Result> response = handleRequest(request(),null,HEALTH_ACTOR_OPERATION_NAME);
		  return response;
	  }

	  /**
	   * This action method is responsible to check certs-service health
	   *
	   * @return a CompletableFuture of success response
	   */
	  public CompletionStage<Result> getServiceHealth(String health) {
	    CompletableFuture<JsonNode> cf = new CompletableFuture<>();
	    Response response = new Response();
	    response.put(RESPONSE, SUCCESS);
	    cf.complete(Json.toJson(response));
	    return service.equalsIgnoreCase(health)
	        ? cf.thenApplyAsync(Results::ok)
	        : cf.thenApplyAsync(Results::badRequest);
	  }
}
