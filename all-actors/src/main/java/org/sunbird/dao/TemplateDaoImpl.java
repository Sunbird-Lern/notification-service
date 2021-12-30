package org.sunbird.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jnr.ffi.annotations.Synchronized;
import org.sunbird.cassandra.CassandraOperation;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.response.Response;
import org.sunbird.common.util.JsonKey;
import org.sunbird.pojo.ActionTemplate;
import org.sunbird.pojo.NotificationTemplate;
import org.sunbird.util.DBUtil;
import org.sunbird.utils.ServiceFactory;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
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
        return cassandraOperation.insertRecord(KEY_SPACE_NAME, NOTIFICATION_TEMPLATE, map, reqContext);

    }

    @Override
    public Response listTemplate(Map<String, Object> reqContext) throws BaseException {
        return cassandraOperation.getAllRecords(KEY_SPACE_NAME,NOTIFICATION_TEMPLATE,reqContext);
    }

    @Override
    public Response updateTemplate(NotificationTemplate template, Map<String, Object> reqContext) throws BaseException {
        Map<String, Object> map = mapper.convertValue(template, Map.class);
        map.remove(JsonKey.TEMPLATE_ID);
        map.put(JsonKey.LAST_UPDATED_ON, new Timestamp(Calendar.getInstance().getTime().getTime()));
        Map<String,Object> compositeKey = new HashMap<>();
        compositeKey.put(JsonKey.TEMPLATE_ID,template.getTemplateId());
        return cassandraOperation.updateRecord(KEY_SPACE_NAME, NOTIFICATION_TEMPLATE, map, compositeKey, reqContext);
    }

    @Override
    public Response deleteTemplate(String templateId, Map<String, Object> reqContext) throws BaseException {
        Map<String,String> compositeKey = new HashMap<>();
        compositeKey.put(JsonKey.TEMPLATE_ID,templateId);
        return cassandraOperation.deleteRecord(KEY_SPACE_NAME, NOTIFICATION_TEMPLATE, compositeKey, reqContext);
    }

    @Override
    public Response upsertActionTemplate(ActionTemplate actionTemplate, Map<String, Object> reqContext) throws BaseException {
        Map<String, Object> map = mapper.convertValue(actionTemplate, Map.class);
        return cassandraOperation.upsertRecord(KEY_SPACE_NAME,ACTION_TEMPLATE,map,reqContext);
    }

    @Override
    public Response getTemplate(String templateId, Map<String,Object> reqContext) throws BaseException {
        return cassandraOperation.getRecordsByProperty(KEY_SPACE_NAME,NOTIFICATION_TEMPLATE, org.sunbird.JsonKey.TEMPLATE_ID,templateId,reqContext);

    }
    @Override
    public Response getTemplateId(String actionType, Map<String,Object> reqContext) throws BaseException {
        return cassandraOperation.getRecordsByProperty(KEY_SPACE_NAME,ACTION_TEMPLATE, org.sunbird.JsonKey.ACTION,actionType,reqContext);
    }
}
