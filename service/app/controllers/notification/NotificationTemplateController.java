package controllers.notification;

import controllers.BaseController;
import controllers.JsonKey;
import controllers.ResponseHandler;
import org.sunbird.common.request.Request;
import org.sunbird.request.LoggerUtil;
import play.mvc.Http;
import play.mvc.Result;
import utils.RequestMapper;
import validators.TemplateActionUpdateRequestValidator;
import validators.TemplateRequestValidator;
import validators.TemplateUpdateRequestValidator;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class NotificationTemplateController extends BaseController {

    private static LoggerUtil logger = new LoggerUtil(NotificationTemplateController.class);

    public CompletionStage<Result> listTemplate(Http.Request req) {
        logger.debug("method call started for listTemplate");
        Request request = new Request();
        try {
            request = RequestMapper.createSBRequest(req);
            CompletionStage<Result> response = handleRequest(request, null, JsonKey.LIST_TEMPLATE, req);
            logger.info("Method call end for listTemplate");
            return response;
        }catch (Exception ex){
            return CompletableFuture.completedFuture(
                    ResponseHandler.handleFailureResponse(request, ex, httpExecutionContext, req));
        }
    }

    public CompletionStage<Result> createTemplate(Http.Request req) {
        logger.debug("method call started for listTemplate");
        Request request = new Request();
        try {
            request = RequestMapper.createSBRequest(req);
            CompletionStage<Result> response = handleRequest(request, new TemplateRequestValidator(), JsonKey.CREATE_TEMPLATE, req);
            logger.info("Method call end for listTemplate");
            return response;
        }catch (Exception ex){
            return CompletableFuture.completedFuture(
                    ResponseHandler.handleFailureResponse(request, ex, httpExecutionContext, req));
        }
    }

    public CompletionStage<Result> updateTemplate(Http.Request req) {
        logger.debug("method call started for updateTemplate");
        Request request = new Request();
        try {
            request = RequestMapper.createSBRequest(req);
            CompletionStage<Result> response = handleRequest(request, new TemplateUpdateRequestValidator(), JsonKey.UPDATE_TEMPLATE, req);
            logger.info("Method call end for updateTemplate");
            return response;
        }catch (Exception ex){
            return CompletableFuture.completedFuture(
                    ResponseHandler.handleFailureResponse(request, ex, httpExecutionContext, req));
        }
    }

    public CompletionStage<Result> deleteTemplate(Http.Request req) {
        logger.debug("method call started for deleteTemplate");
        Request request = new Request();
        try {
            request = RequestMapper.createSBRequest(req);
            CompletionStage<Result> response = handleRequest(request, new TemplateUpdateRequestValidator(), JsonKey.DELETE_TEMPLATE, req);
            logger.info("Method call end for deleteTemplate");
            return response;
        }catch (Exception ex){
            return CompletableFuture.completedFuture(
                    ResponseHandler.handleFailureResponse(request, ex, httpExecutionContext, req));
        }
    }

    public CompletionStage<Result> upsertActionTemplate(Http.Request req) {
        logger.debug("method call started for listTemplate");
        Request request = new Request();
        try {
            request = RequestMapper.createSBRequest(req);
            CompletionStage<Result> response = handleRequest(request, new TemplateActionUpdateRequestValidator(), JsonKey.MAP_ACTION_TEMPLATE, req);
            logger.info("Method call end for listTemplate");
            return response;
        }catch (Exception ex){
            return CompletableFuture.completedFuture(
                    ResponseHandler.handleFailureResponse(request, ex, httpExecutionContext, req));
        }
    }

    public CompletionStage<Result> getAction(String action, Http.Request req) {
        logger.debug("method call started for listTemplate");
        Request request = new Request();
        try {
            request =RequestMapper.createSBRequest(req);
            request.getRequest().put(JsonKey.ACTION, action);
            CompletionStage<Result> response = handleRequest(request, null, JsonKey.READ_ACTION_TEMPLATE, req);
            logger.info("Method call end for listTemplate");
            return response;
        }catch (Exception ex){
            return CompletableFuture.completedFuture(
                    ResponseHandler.handleFailureResponse(request, ex, httpExecutionContext, req));
        }
    }
}
