package controllers;

import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.UntypedAbstractActor;
import org.sunbird.common.response.Response;

public class DummyActor extends UntypedAbstractActor {

    @Override
    public void onReceive(Object message) throws Throwable {
        Response response = new Response();
        sender().tell(response, ActorRef.noSender());
    }
}
