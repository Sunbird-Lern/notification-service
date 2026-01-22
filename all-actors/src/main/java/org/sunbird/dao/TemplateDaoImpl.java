package org.sunbird.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sunbird.cassandra.CassandraOperation;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.request.RequestContext;
import org.sunbird.common.response.Response;
import org.sunbird.common.util.JsonKey;
import org.sunbird.pojo.ActionTemplate;
import org.sunbird.pojo.NotificationTemplate;
import org.sunbird.helper.ServiceFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateDaoImpl implements TemplateDao{

    private static final String KEY_SPACE_NAME = "sunbird_notifications";
    private static final String NOTIFICATION_TEMPLATE= "notification_template";
    private static final String ACTION_TEMPLATE= "action_template";

    private CassandraOperation cassandraOperation = ServiceFactory.getInstance();
    private ObjectMapper mapper = new ObjectMapper();

    private static TemplateDao templateDao = null;

    public static TemplateDao getInstance() {
        if (templateDao == null) {
            templateDao = new TemplateDaoImpl();
        }

        return templateDao;
    }

    @Override
    public Response createTemplate(NotificationTemplate template, Map<String, Object> reqContext) throws BaseException {
        Map<String, Object> map =
                mapper.convertValue(template, new TypeReference<Map<String, Object>>() {});
        map.put(JsonKey.CREATED_ON, new Timestamp(Calendar.getInstance().getTime().getTime()));
        return cassandraOperation.insertRecord(KEY_SPACE_NAME, NOTIFICATION_TEMPLATE, map, getRequestContext(reqContext));

    }

    @Override
    public Response listTemplate(Map<String, Object> reqContext) throws BaseException {
        return cassandraOperation.getAllRecords(KEY_SPACE_NAME,NOTIFICATION_TEMPLATE,getRequestContext(reqContext));
    }

    @Override
    public Response updateTemplate(NotificationTemplate template, Map<String, Object> reqContext) throws BaseException {
        Map<String, Object> map = mapper.convertValue(template, Map.class);
        map.remove(JsonKey.TEMPLATE_ID);
        map.put(JsonKey.LAST_UPDATED_ON, new Timestamp(Calendar.getInstance().getTime().getTime()));
        Map<String,Object> compositeKey = new HashMap<>();
        compositeKey.put(JsonKey.TEMPLATE_ID,template.getTemplateId());
        return cassandraOperation.updateRecord(KEY_SPACE_NAME, NOTIFICATION_TEMPLATE, map, compositeKey, getRequestContext(reqContext));
    }

    @Override
    public Response deleteTemplate(String templateId, Map<String, Object> reqContext) throws BaseException {
        Map<String,String> compositeKey = new HashMap<>();
        compositeKey.put(JsonKey.TEMPLATE_ID,templateId);
        // deleteRecord expects Map<String, String>, NOT Map<String, Object> for composite key. So this is fine.
        return cassandraOperation.deleteRecord(KEY_SPACE_NAME, NOTIFICATION_TEMPLATE, compositeKey, getRequestContext(reqContext));
    }

    @Override
    public Response upsertActionTemplate(ActionTemplate actionTemplate, Map<String, Object> reqContext) throws BaseException {
        Map<String, Object> map = mapper.convertValue(actionTemplate, Map.class);
        return cassandraOperation.upsertRecord(KEY_SPACE_NAME,ACTION_TEMPLATE,map,getRequestContext(reqContext));
    }

    @Override
    public Response getTemplate(String templateId, Map<String,Object> reqContext) throws BaseException {
        List<Object> values = new ArrayList<>();
        values.add(templateId);
        return cassandraOperation.getRecordsByProperty(KEY_SPACE_NAME,NOTIFICATION_TEMPLATE, org.sunbird.JsonKey.TEMPLATE_ID,values,getRequestContext(reqContext));

    }
    @Override
    public Response getTemplateId(String actionType, Map<String,Object> reqContext) throws BaseException {
        List<Object> values = new ArrayList<>();
        values.add(actionType);
        return cassandraOperation.getRecordsByProperty(KEY_SPACE_NAME,ACTION_TEMPLATE, org.sunbird.JsonKey.ACTION,values,getRequestContext(reqContext));
    }

    private RequestContext getRequestContext(Map<String, Object> reqContext) {
        RequestContext requestContext = new RequestContext();
        if (reqContext != null) {
            requestContext.setReqId((String) reqContext.get(JsonKey.REQUEST_ID));
            requestContext.setActorId((String) reqContext.get(JsonKey.ACTOR_ID));
            requestContext.setDid((String) reqContext.get(JsonKey.DEVICE_ID));
            requestContext.setAppId((String) reqContext.get(JsonKey.APP_ID));
            requestContext.getContextMap().putAll(reqContext);
        }
        return requestContext;
    }
}
