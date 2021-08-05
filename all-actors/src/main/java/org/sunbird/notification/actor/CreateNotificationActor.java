package org.sunbird.notification.actor;


import org.sunbird.BaseActor;
import org.sunbird.JsonKey;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.request.Request;
import org.sunbird.response.Response;

@ActorConfig(
        tasks = {JsonKey.CREATE_NOTIFICATION},
        asyncTasks = {}
)
public class CreateNotificationActor extends BaseActor {
    @Override
    public void onReceive(Request request) throws Throwable {
        Response response = new Response();
        sender().tell(response, getSelf());
    }
}

