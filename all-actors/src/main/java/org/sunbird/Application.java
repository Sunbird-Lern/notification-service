package org.sunbird;

import akka.actor.ActorRef;
import org.sunbird.actor.core.ActorCache;
import org.sunbird.actor.core.ActorService;

import java.util.ArrayList;
import java.util.List;

/**
 * this class is used to instantiate the actor system and open saber.
 * @author Amit Kumar
 */
public class Application {
    private final static String actorSystemName = "notificationActorSystem";
    private static Application instance = new Application();

    // private constructor restricted to this class itself
    private Application() {
    }

    // static method to create instance of ActorService class
    public static Application getInstance() {
        return instance;
    }

    // instantiate actor system and actors
    public void init() {
        List<String> actorClassPaths = new ArrayList<>();
        actorClassPaths.add("org.sunbird");
        ActorService.getInstance().init(actorSystemName, actorClassPaths);
    }


    /**
     * this method is used to get the reference of actor from in memory cache.
     * @param operation
     * @return
     */
    public ActorRef getActorRef(String operation) {
        return ActorCache.getActorRef(operation);
    }
}
