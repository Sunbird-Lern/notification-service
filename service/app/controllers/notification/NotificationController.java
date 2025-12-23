/** */
package controllers.notification;

import controllers.BaseController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import controllers.JsonKey;
import controllers.ResponseHandler;
import org.sunbird.NotificationValidator;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.request.Request;
import org.sunbird.request.LoggerUtil;
import play.mvc.Http;
import play.mvc.Result;
import utils.RequestMapper;
import validators.RequestValidator;

/**
 * This controller will be responsible for different kind of notification handling.
 *
 * @author manzarul
 */
public class NotificationController extends BaseController {
  private static LoggerUtil logger = new LoggerUtil(NotificationController.class);

  public static final String NOTIFICATION = "notification";

  /**
   * This method will accept request for sending notification. notification can be sent on email,
   * sms or push on device
   *
   * @return a CompletableFuture of success response
   */
  public CompletionStage<Result> sendNotification(Http.Request req) {
    logger.info("method call started for sendNotification ");
    Request request = new Request();
    try {
      request = RequestMapper.createSBRequest(req);
      CompletionStage<Result> response = handleRequest(request, null, NOTIFICATION, req);
      logger.info("Method call end for sendNotification");
      return response;
    }catch (Exception ex){
      return CompletableFuture.completedFuture(
              ResponseHandler.handleFailureResponse(request, ex, httpExecutionContext, req));
    }

  }

  /**
   * This method will accept request for sending sync notification. notification can be sent on
   * email, sms or push on device
   *
   * @return a CompletableFuture of success response
   */
  public CompletionStage<Result> sendSyncNotification(Http.Request req) {
    logger.info("method call started for sendNotification ");
    req.getHeaders().addHeader(NOTIFICATION_DELIVERY_MODE, "sync");
    Request request = new Request();
    try {
      request = RequestMapper.createSBRequest(req);
      CompletionStage<Result> response = handleRequest(request, null, NOTIFICATION, req);
      logger.info("Method call end for sendNotification");
      return response;
    }catch (Exception ex){
      return CompletableFuture.completedFuture(
              ResponseHandler.handleFailureResponse(request, ex, httpExecutionContext, req));
    }

  }

  /**
   * This method will be used to verify otp.
   *
   * @return
   */
  public CompletionStage<Result> verifyOTP(Http.Request req) {
    logger.info("method call started for verifyOTP ");
    Request request = new Request();
    try {
      request = RequestMapper.createSBRequest(req);
      CompletionStage<Result> response = handleRequest(request, null, JsonKey.VERIFY_OTP, req);
      logger.info("Method call end for verifyOTP");
      return response;
    }catch (Exception ex){
      return CompletableFuture.completedFuture(ResponseHandler.handleFailureResponse(request, ex, httpExecutionContext, req));
    }

  }


  /**
   * This method will accept request for sending new v2 notification. notification can be sent on
   * email, sms, Feed or push on device
   *
   * @return a CompletableFuture of success response
   */
  public CompletionStage<Result> sendV2Notification(Http.Request req) {
    logger.info("method call started for sendNotification ");
    Request request = new Request();
    try {
      request = RequestMapper.createSBRequest(req);
      CompletionStage<Result> response = handleRequest(request, new RequestValidator(), JsonKey.CREATE_NOTIFICATION, req);
      logger.info("Method call end for v2 sendNotification");
      return response;
    }catch (Exception ex){
      return CompletableFuture.completedFuture(
              ResponseHandler.handleFailureResponse(request,ex, httpExecutionContext, req));
    }

  }

  /**
   * This method will accept reading the notification.
   *
   *
   * @return a CompletableFuture of success response
   */
  public CompletionStage<Result> readFeedNotification(String userId, Http.Request req) {
    logger.info("method call started for read Notification Feed ");
    Request request = new Request();
    try {
      request =RequestMapper.createSBRequest(req);
      request.getRequest().put(JsonKey.USER_ID, userId);
      CompletionStage<Result> response = handleRequest(request, null, JsonKey.READ_FEED, req);
      logger.info("Method call end for read Notification Feed");
      return response;
    }catch (BaseException ex){
      return CompletableFuture.completedFuture(
              ResponseHandler.handleFailureResponse(request, ex, httpExecutionContext, req));
    }

  }

  /**
   * This method will accept update the notification.
   *
   *
   * @return a CompletableFuture of success response
   */
  public CompletionStage<Result> updateNotificationFeed(Http.Request req) {
    logger.info("method call started for read Notification Feed ");
    Request request = new Request();
    try {
      request = RequestMapper.createSBRequest(req);
      CompletionStage<Result> response = handleRequest(request, null, JsonKey.UPDATE_FEED, req);
      logger.info("Method call end for read Notification Feed");
      return response;
    }catch (Exception ex){
      return CompletableFuture.completedFuture(
              ResponseHandler.handleFailureResponse(request, ex, httpExecutionContext, req));
    }

  }

  /**
   * This method will accept update the notification.
   *
   *
   * @return a CompletableFuture of success response
   */
  public CompletionStage<Result> updateV1NotificationFeed(Http.Request req) {
    logger.info("method call started for read Notification Feed ");
    Request request = new Request();
    try {
      request = RequestMapper.createSBRequest(req);
      CompletionStage<Result> response = handleRequest(request, null, JsonKey.UPDATE_V1_FEED, req);
      logger.info("Method call end for read Notification Feed");
      return response;
    }catch (Exception ex){
      return CompletableFuture.completedFuture(
              ResponseHandler.handleFailureResponse(request, ex, httpExecutionContext, req));
    }

  }
  

  /**
   * This method will accept reading the notification and return v1 format feeds to support old mobile apps.
   *
   *
   * @return a CompletableFuture of success response
   */
  public CompletionStage<Result> readV1FeedNotification(String userId, Http.Request req) {
    logger.info("method call started for read Notification Feed ");
    Request request = new Request();
    try {
      request =RequestMapper.createSBRequest(req);
      request.getRequest().put(JsonKey.USER_ID, userId);
      request.getRequest().put(JsonKey.VERSION,"v1");
      CompletionStage<Result> response = handleRequest(request, null, JsonKey.READ_V1_FEED, req);
      logger.info("Method call end for read Notification Feed");
      return response;
    }catch (BaseException ex){
      return CompletableFuture.completedFuture(
              ResponseHandler.handleFailureResponse(request, ex, httpExecutionContext, req));
    }

  }

  /**
   * This method will accept request for sending Old v1 notification Feed.
   *
   * @return a CompletableFuture of success response
   */
  public CompletionStage<Result> sendV1Notification(Http.Request req) {
    logger.info("method call started for sendNotification ");
    Request request = new Request();
    try {
      request = RequestMapper.createSBRequest(req);
      request.getRequest().put(JsonKey.VERSION,"v1");
      CompletionStage<Result> response = handleRequest(request, null, JsonKey.CREATE_NOTIFICATION, req);
      logger.info("Method call end for v2 sendNotification");
      return response;
    }catch (Exception ex){
      return CompletableFuture.completedFuture(
              ResponseHandler.handleFailureResponse(request,ex, httpExecutionContext, req));
    }

  }

  /**
   * This method will accept request for delete Old v1 notification Feed.
   *
   * @return a CompletableFuture of success response
   */
  public CompletionStage<Result> deleteV1Notification(Http.Request req) {
    logger.info("method call started for sendNotification ");
    Request request = new Request();
    try {
      request = RequestMapper.createSBRequest(req);
      NotificationValidator.validateDeleteRequest(request);
      CompletionStage<Result> response = handleRequest(request, null, JsonKey.DELETE_V1_FEED, req);
      logger.info("Method call end for v2 sendNotification");
      return response;
    }catch (Exception ex){
      return CompletableFuture.completedFuture(
              ResponseHandler.handleFailureResponse(request,ex, httpExecutionContext, req));
    }

  }

  /**
   * This method will accept request for delete v2 notification Feed.
   *
   * @return a CompletableFuture of success response
   */
  public CompletionStage<Result> deleteNotification(Http.Request req) {
    logger.info("method call started for sendNotification ");
    Request request = new Request();
    try {
      request = RequestMapper.createSBRequest(req);
      NotificationValidator.validateDeleteRequest(request);
      CompletionStage<Result> response = handleRequest(request, null, JsonKey.DELETE_FEED, req);
      logger.info("Method call end for v2 sendNotification");
      return response;
    }catch (Exception ex){
      return CompletableFuture.completedFuture(
              ResponseHandler.handleFailureResponse(request,ex, httpExecutionContext, req));
    }

  }

}
