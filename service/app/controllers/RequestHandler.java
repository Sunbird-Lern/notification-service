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
import org.sunbird.message.ResponseCode;
import org.sunbird.request.Request;
import org.sunbird.response.Response;
import org.sunbird.response.ResponseParams;
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
        request.setOperation(operation);
        Function<Object, Result> fn =
                object -> handleResponse(object, httpExecutionContext, req);

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