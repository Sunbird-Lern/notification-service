package validators;

import org.sunbird.JsonKey;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.request.Request;
import org.sunbird.common.response.Response;
import org.sunbird.request.LoggerUtil;
import utils.ValidationUtil;

import java.util.Arrays;

public class TemplateRequestValidator implements RequestValidatorFunction<Request, Response> {
    private static LoggerUtil logger = new LoggerUtil(TemplateRequestValidator.class);
    @Override
    public Response apply(Request request) throws BaseException {
        try{
            ValidationUtil.validateRequestObject(request);
            ValidationUtil.validateMandatoryParamsWithType(request.getRequest(), Arrays.asList(JsonKey.TYPE,
                    JsonKey.DATA,JsonKey.TEMPLATE_SCHEMA,JsonKey.TEMPLATE_ID),String.class,true,JsonKey.REQUEST,request.getContext());
        }catch (Exception ex){
            logger.error(request.getContext(),"Validation error",ex);
            throw new BaseException(IResponseMessage.Key.INVALID_REQUESTED_DATA, ex.getMessage(), ResponseCode.BAD_REQUEST.getCode());

        }
        return null;
    }
}
