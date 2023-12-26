package org.sunbird.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.response.Response;
import org.sunbird.common.util.JsonKey;
import org.sunbird.dao.TemplateDao;
import org.sunbird.dao.TemplateDaoImpl;
import org.sunbird.pojo.ActionTemplate;
import org.sunbird.pojo.NotificationTemplate;
import org.sunbird.request.LoggerUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TemplateServiceImpl implements TemplateService{
    private static LoggerUtil logger = new LoggerUtil(TemplateServiceImpl.class);
    private static TemplateDao templateDao = TemplateDaoImpl.getInstance();
    private static TemplateService templateService = null;
    private ObjectMapper  mapper = new ObjectMapper();
    public static TemplateService getInstance() {
        if (templateService == null) {
            templateService = new TemplateServiceImpl();
        }
        return templateService;
    }


    @Override
    public  List<NotificationTemplate>  listTemplate(Map<String,Object> reqContext) throws BaseException {
        Response response = templateDao.listTemplate(reqContext);
        if (null != response && MapUtils.isNotEmpty(response.getResult())) {
            List<Map<String, Object>> templateDetails =
                    (List<Map<String, Object>>) response.getResult().get(JsonKey.RESPONSE);
            if(CollectionUtils.isNotEmpty(templateDetails)){
                List<NotificationTemplate> templateDetailList = templateDetails.stream().map(x -> mapper.convertValue(x, NotificationTemplate.class))
                        .collect(Collectors.toList());
                return templateDetailList;
            }
        }
        return new ArrayList<>();
    }

    @Override
    public Response createTemplate(NotificationTemplate notificationTemplate, Map<String,Object> reqContext) {
        return templateDao.createTemplate(notificationTemplate,reqContext);
    }

    @Override
    public Response updateTemplate(NotificationTemplate notificationTemplate, Map<String,Object> reqContext) {
        return templateDao.updateTemplate(notificationTemplate,reqContext);
    }

    @Override
    public Response deleteTemplate(String templateId, Map<String,Object> reqContext) {
        return templateDao.deleteTemplate(templateId,reqContext);
    }

    @Override
    public Response upsertActionTemplate(ActionTemplate actionTemplate,Map<String,Object> reqContext) throws BaseException {
        Response response = templateDao.upsertActionTemplate(actionTemplate,reqContext);
        return response;
    }

    @Override
    public Map<String,Object> getActionTemplate(String action, Map<String,Object> reqContext) throws BaseException {
        Map<String,Object> actionDetails = new HashMap<>();
        Response actionResponseObj =templateDao.getTemplateId(action,reqContext);
        if (null != actionResponseObj) {
            actionDetails.put(JsonKey.ACTION,action);
            if(MapUtils.isEmpty(actionResponseObj.getResult())){
                throw new BaseException(IResponseMessage.TEMPLATE_NOT_FOUND, MessageFormat.format(IResponseMessage.Message.TEMPLATE_NOT_FOUND,action), ResponseCode.CLIENT_ERROR.getResponseCode());
            }
            List<Map<String, Object>> templateIdDetails =
                    (List<Map<String, Object>>) actionResponseObj.getResult().get(JsonKey.RESPONSE);
            if (CollectionUtils.isNotEmpty(templateIdDetails)) {
                Map<String, Object> dbTemplateIdDetail = templateIdDetails.get(0);
                String templateId = (String) dbTemplateIdDetail.get(JsonKey.TEMPLATE_ID);
                Response templateDetailResponseObj = templateDao.getTemplate(templateId,reqContext);
                actionDetails.put(JsonKey.TYPE,dbTemplateIdDetail.get(JsonKey.TYPE));
                Map<String, Object> templateDetailMap = new HashMap<>();
                if (null != templateDetailResponseObj && MapUtils.isNotEmpty(templateDetailResponseObj.getResult())) {
                    List<Map<String, Object>> templateDetails =
                            (List<Map<String, Object>>) templateDetailResponseObj.getResult().get(JsonKey.RESPONSE);
                    if (CollectionUtils.isNotEmpty(templateIdDetails)) {
                        templateDetailMap = templateDetails.get(0);
                    }
                }
                actionDetails.put(JsonKey.TEMPLATE,templateDetailMap);
            }else{
                throw new BaseException(IResponseMessage.TEMPLATE_NOT_FOUND,MessageFormat.format(IResponseMessage.Message.TEMPLATE_NOT_FOUND,action), ResponseCode.CLIENT_ERROR.getResponseCode());
            }
        }else{
            throw new BaseException(IResponseMessage.INTERNAL_ERROR,IResponseMessage.SERVER_ERROR, ResponseCode.SERVER_ERROR.getCode());
        }
        return actionDetails;
    }


}
