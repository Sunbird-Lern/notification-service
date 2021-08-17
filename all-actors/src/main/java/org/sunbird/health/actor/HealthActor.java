/**
 * 
 */
package org.sunbird.health.actor;

import org.sunbird.BaseActor;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.common.request.Request;
import org.sunbird.common.response.Response;
/**
 * @author manzarul
 *
 */
@ActorConfig(
        tasks = {"health"},
        asyncTasks = {}
)
public class HealthActor extends BaseActor{

	@Override
	public void onReceive(Request request) throws Throwable {
		Response response = new Response();
		response.getResult().put("response", "Success");
		sender().tell(response, getSelf());
	}

}
