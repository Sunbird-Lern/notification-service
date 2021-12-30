package org.sunbird.notification.actor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.JsonKey;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.request.Request;
import org.sunbird.common.response.Response;
import org.sunbird.pojo.ActionTemplate;
import org.sunbird.pojo.NotificationTemplate;
import org.sunbird.request.LoggerUtil;
import org.sunbird.BaseActor;
import org.sunbird.service.TemplateService;
import org.sunbird.service.TemplateServiceImpl;
import org.sunbird.telemetry.TelemetryEnvKey;
import org.sunbird.telemetry.util.TelemetryUtil;
import org.sunbird.util.RequestHandler;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ActorConfig(
        tasks = {JsonKey.LIST_TEMPLATE, JsonKey.UPDATE_TEMPLATE,
                JsonKey.CREATE_TEMPLATE, JsonKey.DELETE_TEMPLATE,JsonKey.READ_ACTION_TEMPLATE,
                JsonKey.MAP_ACTION_TEMPLATE},
        asyncTasks = {},
        dispatcher= "notification-dispatcher"
)
public class NotificationTemplateActor extends BaseActor {
    private static LoggerUtil logger = new LoggerUtil(NotificationTemplateActor.class);
    @Override
    public void onReceive(Request request) throws Throwable {
        String operation = request.getOperation();
        if (JsonKey.CREATE_TEMPLATE.equalsIgnoreCase(operation)) {
               createTemplate(request);
        } else if (JsonKey.UPDATE_TEMPLATE.equalsIgnoreCase(operation)) {
               updateTemplate(request);
        } else if (JsonKey.LIST_TEMPLATE.equalsIgnoreCase(operation)) {
               listTemplate(request);
        } else if (JsonKey.DELETE_TEMPLATE.equalsIgnoreCase(operation)) {
               deleteTemplate(request);
        } else if(JsonKey.MAP_ACTION_TEMPLATE.equals(operation)){
               mapActionTemplate(request);
        }else if(JsonKey.READ_ACTION_TEMPLATE.equals(operation)){
               readActionTemplate(request);
        } else {
            onReceiveUnsupportedMessage(request.getOperation());
        }
        logger.info(request.getContext(),"onReceive method call End");
    }

    private void readActionTemplate(Request request) {
        logger.info(request.getContext(),"Call started for read Action Template Method");
        TemplateService templateService = TemplateServiceImpl.getInstance();
        try {
            Response response = new Response();
            String action  = (String) request.getRequest().get(JsonKey.ACTION);
            Map<String,Object> actionResultMap = templateService.getActionTemplate(action,request.getContext());
            response.putAll(actionResultMap);
            logger.info(request.getContext(), "response got from notification service " + response);
            sender().tell(response, getSelf());
        }catch (BaseException ex){
            logger.error(MessageFormat.format(":Error Msg: {0} ",ex.getMessage()),
                    ex);
            throw ex;
        } catch (Exception ex){
            logger.error(MessageFormat.format("NotificationTemplateActor:Error Msg: {0} ",ex.getMessage()),
                    ex);
            throw new BaseException(IResponseMessage.Key.SERVER_ERROR,IResponseMessage.Message.INTERNAL_ERROR, ResponseCode.serverError.getResponseCode());
        }
    }

    private void mapActionTemplate(Request request) {
        logger.info(request.getContext(),"Call started for map action Template Method");
        ObjectMapper mapper = new ObjectMapper();
        TemplateService templateService = TemplateServiceImpl.getInstance();
        try {
            Response response = new Response();
            Map<String, Object> actionTemplateMap = (Map<String, Object>) request.getRequest();
            ActionTemplate template = mapper.convertValue(actionTemplateMap, ActionTemplate.class);
            response = templateService.upsertActionTemplate(template,request.getContext());
            logger.info(request.getContext(), "response got from notification service " + response);
            sender().tell(response, getSelf());
        }catch (BaseException ex){
            logger.error(MessageFormat.format(":Error Msg: {0} ",ex.getMessage()),
                    ex);
            throw ex;
        } catch (Exception ex){
            logger.error(MessageFormat.format("NotificationTemplateActor:Error Msg: {0} ",ex.getMessage()),
                    ex);
            throw new BaseException(IResponseMessage.Key.SERVER_ERROR,IResponseMessage.Message.INTERNAL_ERROR, ResponseCode.serverError.getResponseCode());
        }
    }

    private void deleteTemplate(Request request) {
        logger.info(request.getContext(),"Call started for delete Template Method");
        TemplateService templateService = TemplateServiceImpl.getInstance();
        try {
            Response response = new Response();
            String templateId  = (String) request.getRequest().get(JsonKey.TEMPLATE_ID);
            templateService.deleteTemplate(templateId,request.getContext());
            logger.info(request.getContext(), "response got from notification service " + response);
            sender().tell(response, getSelf());
        }catch (BaseException ex){
            logger.error(MessageFormat.format(":Error Msg: {0} ",ex.getMessage()),
                    ex);
            throw ex;
        } catch (Exception ex){
            logger.error(MessageFormat.format("NotificationTemplateActor:Error Msg: {0} ",ex.getMessage()),
                    ex);
            throw new BaseException(IResponseMessage.Key.SERVER_ERROR,IResponseMessage.Message.INTERNAL_ERROR, ResponseCode.serverError.getResponseCode());
        }
    }

    private void listTemplate(Request request) {
        logger.info(request.getContext(),"Call started for list Template Method");
        TemplateService templateService = TemplateServiceImpl.getInstance();
        try {
            Response response = new Response();
            List<NotificationTemplate> templates = templateService.listTemplate(request.getContext());
            response.put(JsonKey.TEMPLATE,templates);
            logger.info(request.getContext(), "response got from notification service " + response);
            sender().tell(response, getSelf());
        }catch (BaseException ex){
            logger.error(MessageFormat.format(":Error Msg: {0} ",ex.getMessage()),
                    ex);
            throw ex;
        } catch (Exception ex){
            logger.error(MessageFormat.format("NotificationTemplateActor:Error Msg: {0} ",ex.getMessage()),
                    ex);
            throw new BaseException(IResponseMessage.Key.SERVER_ERROR,IResponseMessage.Message.INTERNAL_ERROR, ResponseCode.serverError.getResponseCode());
        }
    }

    private void updateTemplate(Request request) {
        logger.info(request.getContext(),"Call started for update Template Method");
        TemplateService templateService = TemplateServiceImpl.getInstance();
        ObjectMapper mapper = new ObjectMapper();
        RequestHandler requestHandler = new RequestHandler();
        String requestedBy = requestHandler.getRequestedBy(request);
        try {
            Map<String, Object> templateNotification = (Map<String, Object>) request.getRequest();
            Response response = new Response();
            if (MapUtils.isNotEmpty(templateNotification)) {
                NotificationTemplate template = mapper.convertValue(templateNotification, NotificationTemplate.class);
                template.setLastUpdatedBy(requestedBy);
                response = templateService.updateTemplate(template, request.getContext());
            }
            logger.info(request.getContext(), "response got from notification service " + response);
            sender().tell(response, getSelf());
        }catch (BaseException ex){
            logger.error(MessageFormat.format(":Error Msg: {0} ",ex.getMessage()),
                    ex);
            throw ex;
        } catch (Exception ex){
            logger.error(MessageFormat.format("NotificationTemplateActor:Error Msg: {0} ",ex.getMessage()),
                    ex);
            throw new BaseException(IResponseMessage.Key.SERVER_ERROR,IResponseMessage.Message.INTERNAL_ERROR, ResponseCode.serverError.getResponseCode());
        }
    }

    private void createTemplate(Request request) {
        logger.info(request.getContext(),"Call started for create Template Method");
        TemplateService templateService = TemplateServiceImpl.getInstance();
        ObjectMapper mapper = new ObjectMapper();
        RequestHandler requestHandler = new RequestHandler();
        String requestedBy = requestHandler.getRequestedBy(request);
        try {
            Map<String, Object> templateNotification = (Map<String, Object>) request.getRequest();

            Response response = new Response();
            if (MapUtils.isNotEmpty(templateNotification)) {
                NotificationTemplate template = mapper.convertValue(templateNotification, NotificationTemplate.class);
                template.setCreatedBy(requestedBy);
                response = templateService.createTemplate(template, request.getContext());
            }
            logger.info(request.getContext(), "response got from notification service " + response);
            sender().tell(response, getSelf());
        }catch (BaseException ex){
            logger.error(MessageFormat.format(":Error Msg: {0} ",ex.getMessage()),
                    ex);
            throw ex;
        } catch (Exception ex){
            logger.error(MessageFormat.format("NotificationTemplateActor:Error Msg: {0} ",ex.getMessage()),
                    ex);
            throw new BaseException(IResponseMessage.Key.SERVER_ERROR,IResponseMessage.Message.INTERNAL_ERROR, ResponseCode.serverError.getResponseCode());
        }
    }

}
