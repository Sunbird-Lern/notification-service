/** */
package controllers.notification;

import controllers.BaseController;
import java.util.concurrent.CompletionStage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import play.mvc.Result;
import utils.JsonKey;

/**
 * This controller will be responsible for different kind of notification handling.
 *
 * @author manzarul
 */
public class NotificationController extends BaseController {
  Logger logger = LogManager.getLogger(NotificationController.class);

  public static final String NOTIFICATION = "notification";

  /**
   * This method will accept request for sending notification. notification can be sent on email,
   * sms or push on device
   *
   * @return a CompletableFuture of success response
   */
  public CompletionStage<Result> sendNotification() {
    logger.info("method call started for sendNotification ");
    CompletionStage<Result> response = handleRequest(request(), null, NOTIFICATION);
    logger.info("Method call end for sendNotification");
    return response;
  }

  /**
   * This method will accept request for sending sync notification. notification can be sent on
   * email, sms or push on device
   *
   * @return a CompletableFuture of success response
   */
  public CompletionStage<Result> sendSyncNotification() {
    logger.info("method call started for sendNotification ");
    request().getHeaders().addHeader(NOTIFICATION_DELIVERY_MODE, "sync");
    CompletionStage<Result> response = handleRequest(request(), null, NOTIFICATION);
    logger.info("Method call end for sendNotification");
    return response;
  }

  /**
   * This method will be used to verify otp.
   *
   * @return
   */
  public CompletionStage<Result> verifyOTP() {
    logger.info("method call started for verifyOTP ");
    CompletionStage<Result> response = handleRequest(request(), null, JsonKey.VERIFY_OTP);
    logger.info("Method call end for verifyOTP");
    return response;
  }
}
