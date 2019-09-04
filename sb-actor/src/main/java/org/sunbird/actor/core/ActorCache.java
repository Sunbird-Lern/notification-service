package org.sunbird.actor.core;

import akka.actor.ActorRef;

import java.util.HashMap;
import java.util.Map;

/**
 * This class will maintain the cache of actor reference.
 * NOTE:: Today does not handle remote actor references
 * @author Amit Kumar
 */
public class ActorCache {

    private ActorCache(){}

    private static Map<String, ActorRef> actorRefCache = new HashMap<>();

    /**
     * This method will return the map of actor operation and actor reference.
     * @return Map of string and actor reference
     */
    public static Map<String, ActorRef> getActorCache() {
        return actorRefCache;
    }

    /**
     * This method will return the actor reference based on actor operation
     * @param actorOperation operation performed by actor
     * @return ActorRef actor reference
     */
    public static ActorRef getActorRef(String actorOperation){
        return actorRefCache.get(actorOperation);
    }
}
