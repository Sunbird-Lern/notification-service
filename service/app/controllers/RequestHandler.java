package controllers;

import akka.pattern.Patterns;
import akka.util.Timeout;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.BaseException;
import org.sunbird.message.IResponseMessage;
import org.sunbird.request.Request;
import org.sunbird.response.Response;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import play.mvc.Results;
import scala.compat.java8.FutureConverters;
import scala.concurrent.Future;
import utils.JsonKey;

/**
 * this class is used to handle the request and ask from actor and return response on the basis of
 * success and failure to user.
 *
 * @author amitkumar
 */
public class RequestHandler extends BaseController {
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
        Object obj;
        CompletableFuture<String> cf = new CompletableFuture<>();
        request.setOperation(operation);
        Function<Object, Result> fn =
                new Function<Object, Result>() {

                    @Override
                    public Result apply(Object object) {
                        return handleResponse(object, httpExecutionContext, req);
                    }
                };

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
            Object exception, HttpExecutionContext httpExecutionContext, play.mvc.Http.Request req) {

        Response response = new Response();
        CompletableFuture<JsonNode> future = new CompletableFuture<>();
        if (exception instanceof BaseException) {
            BaseException ex = (BaseException) exception;
            response.setResponseCode(ex.getCode());
            response.put(JsonKey.MESSAGE, ex.getMessage());
            String apiId = getApiId(req.path());
            response.setId(apiId);
            response.setVer("v1");
            response.setTs(System.currentTimeMillis() + "");
            future.complete(Json.toJson(response));
            if (ex.getResponseCode() == Results.badRequest().status()) {
                return Results.badRequest(Json.toJson(response));
            } else {
                return Results.internalServerError();
            }
        } else {
            response.setResponseCode(IResponseMessage.SERVER_ERROR);
            response.put(
                    JsonKey.MESSAGE, localizerObject.getMessage(IResponseMessage.INTERNAL_ERROR, null));
            future.complete(Json.toJson(response));
            return Results.internalServerError(Json.toJson(response));
        }
    }

    /**
     * this method will divert the response on the basis of success and failure
     *
     * @param object
     * @param httpExecutionContext
     * @return
     */
    public static Result handleResponse(
            Object object, HttpExecutionContext httpExecutionContext, play.mvc.Http.Request req) {

        if (object instanceof Response) {
            Response response = (Response) object;
            return handleSuccessResponse(response, httpExecutionContext, req);
        } else {
            return handleFailureResponse(object, httpExecutionContext, req);
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
}