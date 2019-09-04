/**
 *
 */
package utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.log4j.Logger;
import org.sunbird.ActorServiceException;
import org.sunbird.BaseActor;
import org.sunbird.BaseException;
import org.sunbird.message.IResponseMessage;
import org.sunbird.message.Localizer;
import org.sunbird.message.ResponseCode;
import play.libs.Json;

/**
 * This class will map the requested json data into custom class.
 *
 * @author Manzarul
 */
public class RequestMapper {

    private static Logger logger = Logger.getLogger(BaseActor.class);

    /**
     * Method to map request
     *
     * @param req play mvc request
     * @param obj Class<T>
     * @exception RuntimeException
     * @return <T>
     */
    public static <T> Object mapRequest(play.mvc.Http.Request req, Class<T> obj) throws BaseException {
        logger.info("RequestMapper:mapRequest:Requested data:" + req);
        if (req == null) throw new ActorServiceException.InvalidRequestData(
                IResponseMessage.INVALID_REQUESTED_DATA,
                Localizer.getInstance().getMessage(IResponseMessage.INVALID_REQUESTED_DATA, null),
                ResponseCode.CLIENT_ERROR.getCode());
        JsonNode requestData = null;
        try {
            requestData = req.body().asJson();
            return Json.fromJson(requestData, obj);
        } catch (Exception e) {
            logger.error("RequestMapper:mapRequest: " + e.getMessage(), e);
            logger.info("RequestMapper:mapRequest:Requested data " + requestData);

            throw new ActorServiceException.InvalidRequestData(
                    IResponseMessage.INVALID_REQUESTED_DATA,
                    Localizer.getInstance().getMessage(IResponseMessage.INVALID_REQUESTED_DATA, null),
                    ResponseCode.CLIENT_ERROR.getCode());
        }
    }
}

