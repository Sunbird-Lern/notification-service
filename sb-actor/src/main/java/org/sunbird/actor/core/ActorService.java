package org.sunbird.actor.core;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.FromConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class contains method to instantiate actor system and actors.
 * @author Amit Kumar
 */
public class ActorService {

    private static ActorSystem system;
    private static Config config =
            ConfigFactory.systemEnvironment().withFallback(ConfigFactory.load());
    private static Map<String, ActorRef> actorRefCache = ActorCache.getActorCache();

    // static variable instance of type ActorService
    private static ActorService instance = null;

    // private constructor restricted to this class itself
    private ActorService() { }

    // static method to create instance of ActorService class
    public static ActorService getInstance()
    {
        if (instance == null)
            instance = new ActorService();

        return instance;
    }

    // instantiate actor system and actors
    public void init(String actorSystemName, List<String> actorsClassPathList) {

        getActorSystem(actorSystemName);
        initActors(actorsClassPathList);

    }

    /**
     * This method will instantiate actor system
     * @return actor system
     */
    private ActorSystem getActorSystem(String actorSystemName) {

        if (null == system) {
            Config conf = config.getConfig(actorSystemName);
            system = ActorSystem.create(actorSystemName, conf);
        }

        return system;

    }

    /**
     * initialize the actors
     */
    private void initActors(List<String> actorsClassPathList) {
        Set<Class<?>> actors = getActors(actorsClassPathList);
        for (Class<?> actor : actors) {
            ActorConfig routerDetails = actor.getAnnotation(ActorConfig.class);
            if (null != routerDetails) {
                String[] operations = routerDetails.tasks();
                String dispatcher = (StringUtils.isNotBlank(routerDetails.dispatcher())) ? routerDetails.dispatcher() : "default-dispatcher";
                createActor(actor,operations, dispatcher);
            }
        }
    }


    private Set<Class<?>> getActors(List<String> actorsClassPathList) {
        synchronized (ActorService.class) {
            Reflections reflections = null;
            Set<Class<?>> actors = new HashSet<>();
            for(String classpath : actorsClassPathList){
                reflections = new Reflections(classpath);
                actors.addAll(reflections.getTypesAnnotatedWith(ActorConfig.class));
            }
            return actors;
        }
    }


    private void createActor(Class actor,
            String[] operations,
            String dispatcher) {

        if (null != operations && operations.length > 0) {
            Props props;
            if (StringUtils.isNotBlank(dispatcher)) {
                props = Props.create(actor).withDispatcher(dispatcher);
            } else {
                props = Props.create(actor);
            }
            ActorRef actorRef =
                    system.actorOf(FromConfig.getInstance().props(props), actor.getSimpleName());
            for (String operation : operations) {
                actorRefCache.put(operation, actorRef);
            }
        }
    }

}
