package org.sunbird.dao;

import org.sunbird.common.exception.BaseException;
import org.sunbird.common.response.Response;
import org.sunbird.pojo.ActionTemplate;
import org.sunbird.pojo.NotificationTemplate;

import java.util.Map;

public interface TemplateDao {

    Response createTemplate(NotificationTemplate template, Map<String,Object> reqContext) throws BaseException;

    Response listTemplate(Map<String,Object> reqContext) throws BaseException;

    Response updateTemplate(NotificationTemplate template,Map<String,Object> reqContext ) throws BaseException;

    Response deleteTemplate(String templateId,Map<String,Object> reqContext)throws BaseException;

    Response upsertActionTemplate(ActionTemplate actionTemplate, Map<String,Object> reqContext) throws BaseException;

    Response getTemplateId(String actionType, Map<String,Object> reqContext) throws BaseException;

    Response getTemplate(String templateId, Map<String,Object> reqContext) throws BaseException;

}
