package controllers;

import akka.actor.ActorRef;
import akka.actor.UntypedAbstractActor;
import org.sunbird.common.response.Response;

public class DummyActor extends UntypedAbstractActor {

    @Override
    public void onReceive(Object message) throws Throwable {
        Response response = new Response();
        sender().tell(response, ActorRef.noSender());
    }
}
