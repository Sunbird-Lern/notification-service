package utils;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.exception.ValidationException;
import org.sunbird.common.request.Request;
import org.sunbird.request.LoggerUtil;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

public class ValidationUtil {

    private static LoggerUtil logger = new LoggerUtil(ValidationUtil.class);

    public static void validateRequestObject(Request request) throws BaseException {
        if (request.getRequest().isEmpty()) {
            logger.error(request.getContext(),"validateMandatoryParamsOfStringType:incorrect request provided");
            throw new ValidationException.InvalidRequestData();
        }
    }

    public static void validateMandatoryParamsWithType(
            Map<String, Object> reqMap,
            List<String> mandatoryParamsList,
            Class<?> type,
            boolean validatePresence,
            String parentKey,
            Map<String,Object> reqContext)
            throws BaseException {
        for (String param : mandatoryParamsList) {
            if (!reqMap.containsKey(param)) {
                throw new ValidationException.MandatoryParamMissing(param, parentKey);
            }

            if (!(isInstanceOf(reqMap.get(param).getClass(), type))) {
                logger.error(reqContext,"validateMandatoryParamsOfStringType:incorrect request provided");
                throw new ValidationException.ParamDataTypeError(parentKey + "." + param, type.getName());
            }

            if (validatePresence) {
                validatePresence(param, reqMap.get(param), type, parentKey,reqContext);
            }
        }
    }

    public static void validateParamsWithType(Map<String, Object> reqMap,
                                              List<String> paramList,
                                              Class<?> type,
                                              String parentKey,
                                              Map<String,Object> reqContext)
            throws BaseException {
        for (String param : paramList) {
            if(reqMap.containsKey(param)) {
                if (!(isInstanceOf(reqMap.get(param).getClass(), type))) {
                    logger.error(reqContext,"validateMandatoryParamsType:incorrect request provided");
                    throw new ValidationException.ParamDataTypeError(parentKey + "." + param, type.getName());
                }
            }
        }
    }

    private static void validatePresence(String key, Object value, Class<?> type, String parentKey,Map<String,Object> reqContext)
            throws BaseException {
        if (type == String.class) {
            if (StringUtils.isBlank((String) value)) {
                logger.error(reqContext,"validatePresence:incorrect request provided");
                throw new ValidationException.MandatoryParamMissing(key, parentKey);
            }
        } else if (type == Map.class) {
            Map<String, Object> map = (Map<String, Object>) value;
            if (map.isEmpty()) {
                logger.error(reqContext,"validatePresence:incorrect request provided");
                throw new ValidationException.MandatoryParamMissing(key, parentKey);
            }
        } else if (type == List.class) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) {
                logger.error(reqContext,"validatePresence:incorrect request provided");
                throw new ValidationException.MandatoryParamMissing(key, parentKey);
            }
        }
    }
    /**
     * @param reqMap
     * @param params list of params to validate values it contains
     * @param paramsValue for each params provided , add a values in the map key should be the
     *     paramName , value should be list of paramValue it should be for example key=status
     *     value=[active, inactive]
     * @throws BaseException
     */
    public static void validateParamValue(
            Map<String, Object> reqMap,
            List<String> params,
            Map<String, List<String>> paramsValue,
            String parentKey,
            Map<String,Object> reqContext)
            throws BaseException {
        logger.info(reqContext, MessageFormat.format(
                "validateParamValue: validating Param Value for the params {0} values {1}",
                params,
                paramsValue));
        for (String param : params) {
            if (reqMap.containsKey(param) && StringUtils.isNotEmpty((String) reqMap.get(param))) {
                List<String> values = paramsValue.get(param);
                String paramValue = (String) reqMap.get(param);
                if (!values.contains(paramValue)) {
                    throw new ValidationException.InvalidParamValue(paramValue, parentKey + param);
                }
            }
        }
    }

    public static boolean isInstanceOf(Class objClass, Class targetClass) {
        return targetClass.isAssignableFrom(objClass);
    }
}