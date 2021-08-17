package org.sunbird.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.response.Response;
import org.sunbird.common.util.JsonKey;

public interface UserService {

    public Response getSystemSettings() throws BaseException;

    public Response getOrganisationDetails(String orgId) throws BaseException;

    default void getUpdatedRequestHeader(Map<String, String> header, Map<String, Object> reqContext) {
        if (null == header) {
            header = new HashMap<>();
        }
        header.put("Content-Type", "application/json");
        setTraceIdInHeader(header, reqContext);
    }

    public static void setTraceIdInHeader(Map<String, String> header,  Map<String, Object> reqContext) {
        if (null != reqContext) {
            header.put(JsonKey.X_TRACE_ENABLED, (String) reqContext.get(JsonKey.X_TRACE_ENABLED));
            header.put(JsonKey.X_REQUEST_ID, (String) reqContext.get(JsonKey.X_REQUEST_ID));
        }
    }
}
