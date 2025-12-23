package validators;

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
import play.libs.Json;

public class RequestValidator implements RequestValidatorFunction<Request, Response> {
    private static LoggerUtil logger = new LoggerUtil(RequestValidator.class);
    @Override
    public Response apply(Request request) throws BaseException {
        try {
            JSONObject jsonSchema = new JSONObject(new JSONTokener(
                    getClass().getClassLoader().
                            getResourceAsStream("schemas/notification.json")));
            // Use Play's Json library to properly handle Scala collections
            String jsonString = Json.stringify(Json.toJson(request.getRequest()));
            JSONObject jsonObj = new JSONObject(jsonString);
            Schema schema = SchemaLoader.load(jsonSchema);
            schema.validate(jsonObj);
        } catch (Exception ex){
            logger.error(request.getContext(),"Error during json processing",ex);
            throw new BaseException(IResponseMessage.Key.INVALID_REQUESTED_DATA, ex.getMessage(), ResponseCode.BAD_REQUEST.getCode());
        }
        return null;
    }
}
