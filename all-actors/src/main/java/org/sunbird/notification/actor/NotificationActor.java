/**
 * 
 */
package org.sunbird.notification.actor;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sunbird.BaseActor;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.request.Request;
import org.sunbird.response.Response;

/**
 * @author manzarul
 *
 */

@ActorConfig(
        tasks = {"notification"},
        asyncTasks = {}
)
public class NotificationActor extends BaseActor {
	Logger logger = LogManager.getLogger(NotificationActor.class);
	private static final String NOTIFICATION = "notification";

	@Override
	public void onReceive(Request request) throws Throwable {
		String operation = request.getOperation();
		logger.info("onReceive method call start for operation " + operation);
		if (NOTIFICATION.equalsIgnoreCase(operation)) {
			notify(request);
		} else {
			onReceiveUnsupportedMessage(request.getOperation());
		}

		logger.info("onReceive method call End");
	}

	public void notify(Request request) {
		System.out.println("Success");
		Response response = new Response();
		response.getResult().put("response", "SUCCESS");
		sender().tell(response, getSelf());

	}

}
