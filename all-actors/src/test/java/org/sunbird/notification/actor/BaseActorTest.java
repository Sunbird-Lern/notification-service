package org.sunbird.notification.actor;

import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.sunbird.Application;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.response.Response;
import org.sunbird.common.util.JsonKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PrepareForTest({Application.class})
public abstract class BaseActorTest {

    static Map<String, Object> headerMap = new HashMap<>();
    static ActorSystem system;

    @BeforeClass
    public  static void setup() {
        system = ActorSystem.create("system");
        setReqId();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    private static void setReqId() {
        List<String> reqIds = new ArrayList<>();
        reqIds.add("71ef3311-ac58-49a1-872b-7cf28159de83");
        headerMap.put(JsonKey.REQUEST_MESSAGE_ID, reqIds);
    }

    public static Response getCassandraResponse() {
        Response response = new Response();
        Map<String, Object> result = new HashMap<>();
        result.put(JsonKey.RESPONSE, ResponseCode.OK.getCode());
        response.putAll(result);
        return response;
    }



}
