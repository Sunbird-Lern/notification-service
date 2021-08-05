package org.sunbird.notification.actor;

import org.sunbird.BaseActor;
import org.sunbird.JsonKey;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.request.Request;
import org.sunbird.response.Response;

@ActorConfig(
        tasks = {JsonKey.UPDATE_FEED},
        asyncTasks = {}
)
public class UpdateNotificationActor extends BaseActor {
    @Override
    public void onReceive(Request request) throws Throwable {
        Response response = new Response();
        response.put("result","ok");
        sender().tell(response, getSelf());

    }
}

