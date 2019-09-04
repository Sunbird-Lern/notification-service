/**
 * 
 */
package controllers.notification;

import java.util.concurrent.CompletionStage;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import controllers.BaseController;
import play.mvc.Result;

/**
 * This controller will be responsible for different kind of notification 
 * handling.
 * @author manzarul
 *
 */
public class NotificationController extends BaseController{
	 Logger logger = LogManager.getLogger(NotificationController.class);
	
	public static final String NOTIFICATION = "notification";
	
	/**
	   * This method will accept request for sending notification.
	   * notification can be sent on email, sms or push on device
	   * @return a CompletableFuture of success response
	   */
	  public CompletionStage<Result> sendNotification() {
		logger.info("method call started for sendNotification " + request().body().asJson());
		CompletionStage<Result> response = handleRequest(request(),null,NOTIFICATION);
		logger.info("Method call end for sendNotification");
	    return response;
	  }


}
