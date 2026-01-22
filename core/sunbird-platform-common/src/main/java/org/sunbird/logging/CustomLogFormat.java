package org.sunbird.logging;

import org.sunbird.common.request.RequestContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomLogFormat {
    private String edataType = "system";
    private String eid = "LOG";
    private String ver = "3.0";
    private Map<String, Object> edata = new HashMap<>();
    private Map<String, Object> eventMap = new HashMap<>();

    CustomLogFormat(RequestContext requestContext, String msg, Map<String, Object> object, Map<String, Object> params) {
        if (params != null)
            this.edata.put("params", new ArrayList<Map<String, Object>>(){{add(params);}});
        setEventMap(requestContext, msg);
        if (object != null)
            this.eventMap.put("object", object);
    }

    public Map<String, Object> getEventMap() {
        return this.eventMap;
    }

    public void setEventMap(RequestContext requestContext, String msg) {
        this.edata.put("type", edataType);
        this.edata.put("requestid", requestContext.getRequestId());
        this.edata.put("message", msg);
        this.edata.put("level", requestContext.getLoggerLevel());
        this.eventMap.putAll(new HashMap<String, Object>() {{
            put("eid", eid);
            put("ets", System.currentTimeMillis());
            put("ver", ver);
            put("mid", "LOG:" + UUID.randomUUID().toString());
            put("context", requestContext.getContextMap());
            put("actor", new HashMap<String, Object>() {{
                put("id", requestContext.getActorId());
                put("type", requestContext.getActorType());
            }});
            put("edata", edata);
        }});
    }
}

