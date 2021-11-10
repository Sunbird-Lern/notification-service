package controllers;

import akka.pattern.Patterns;
import akka.util.Timeout;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.request.Request;
import org.sunbird.common.response.Response;
import org.sunbird.common.response.ResponseFactory;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import scala.compat.java8.FutureConverters;
import scala.concurrent.Future;
import utils.PrintEntryExitLog;
import utils.RequestMapper;

/**
 * this class is used to handle the request and ask from actor and return response on the basis of
 * success and failure to user.
 *
 * @author amitkumar
 */
public class ResponseHandler extends BaseController {
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
        request.setOperation(operation);
        Function<Object, Result> fn =
                new Function<Object, Result>() {

                    @Override
                    public Result apply(Object object) {
                        return handleResponse(request, object, httpExecutionContext, req);
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

    public static Result handleFailureResponse(Request request,Object exception, HttpExecutionContext httpExecutionContext, play.mvc.Http.Request req) {
        Result result;
        Response response = ResponseFactory.getFailureMessage(exception, request);
        switch (response.getResponseCode().getCode()) {
            case HttpStatus.SC_BAD_REQUEST:
                result = Results.badRequest(Json.toJson(response));
                break;
            case HttpStatus.SC_UNAUTHORIZED:
                result = Results.unauthorized(Json.toJson(response));
                break;
            default:
                result = Results.internalServerError(Json.toJson(response));
                break;
        }
        PrintEntryExitLog.printExitLogOnFailure(request, (BaseException) exception);
        return result;
    }


    /**
     * This method will handle all the failure response of Api calls.
     *
     * @param
     * @return
     */
    public static Result handleFailureResponse(Object exception,Http.Request request) throws BaseException {
        Result result;
        Request sbReq = RequestMapper.createSBRequest(request);
        result = handleFailureResponse(sbReq, exception,null,request);
        return result;
    }


    /**
     * this method will divert the response on the basis of success and failure
     *
     * @param object
     * @param httpExecutionContext
     * @return
     */
    public static Result handleResponse(
            Request request, Object object, HttpExecutionContext httpExecutionContext, play.mvc.Http.Request req) {

        if (object instanceof Response) {
            Response response = (Response) object;
            return handleSuccessResponse(request, response, httpExecutionContext, req);
        } else {
            return handleFailureResponse(request, object, httpExecutionContext, req);
        }
    }

    /**
     * This method will handle all the success response of Api calls.
     *
     * @param response
     * @return
     */
    public static Result handleSuccessResponse(
          Request request,  Response response, HttpExecutionContext httpExecutionContext, play.mvc.Http.Request req) {
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