package validators;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.request.Request;
import org.sunbird.request.LoggerUtil;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.response.Response;

public class RequestValidator implements RequestValidatorFunction<Request, Response> {
    private static LoggerUtil logger = new LoggerUtil(RequestValidator.class);
    @Override
    public Response apply(Request request) throws BaseException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JSONObject jsonSchema = new JSONObject(new JSONTokener(
                    getClass().getClassLoader().
                            getResourceAsStream("schemas/notification.json")));
            JSONObject jsonObj = new JSONObject(mapper.writeValueAsString(request.getRequest()).toString());
            Schema schema = SchemaLoader.load(jsonSchema);
            schema.validate(jsonObj);
        } catch (JsonProcessingException e) {
            logger.error("Error during json processing",e);
            throw new BaseException(IResponseMessage.SERVER_ERROR, IResponseMessage.SERVER_ERROR, ResponseCode.SERVER_ERROR.getCode());

        }catch (Exception ex){
            logger.error(request.getContext(),"Error during json processing",ex);
            throw new BaseException(IResponseMessage.Key.INVALID_REQUESTED_DATA, ex.getMessage(), ResponseCode.BAD_REQUEST.getCode());
        }
        return null;
    }
}
