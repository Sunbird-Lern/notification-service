package org.sunbird.service;

import org.sunbird.common.exception.BaseException;
import org.sunbird.common.request.RequestContext;
import org.sunbird.common.response.Response;
import org.sunbird.pojo.ActionTemplate;
import org.sunbird.pojo.NotificationTemplate;

import java.util.List;
import java.util.Map;

public interface TemplateService {

    List<NotificationTemplate> listTemplate(Map<String,Object> reqContext) throws BaseException;

    Response createTemplate(NotificationTemplate notificationTemplate, Map<String,Object> reqContext);

    Response updateTemplate(NotificationTemplate notificationTemplate,Map<String,Object> reqContext);

    Response deleteTemplate(String templateId,Map<String,Object> reqContext);

    Response upsertActionTemplate(ActionTemplate actionTemplate, Map<String,Object> reqContext) throws BaseException;

    Map<String,Object> getActionTemplate(String action, Map<String,Object> reqContext) throws BaseException;

}
