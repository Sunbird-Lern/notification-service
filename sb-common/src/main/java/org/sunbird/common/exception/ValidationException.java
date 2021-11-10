package org.sunbird.common.exception;

import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.Localizer;
import org.sunbird.common.message.ResponseCode;

import java.text.MessageFormat;
import java.util.Locale;

public class ValidationException {

    private static Localizer localizer = Localizer.getInstance();

    public static class InvalidRequestData extends BaseException {

        public InvalidRequestData() {
            super(
                    IResponseMessage.INVALID_REQUESTED_DATA,
                   MessageFormat.format(IResponseMessage.INVALID_REQUESTED_DATA, null),
                    400);
        }
    }

    public static class MandatoryParamMissing extends BaseException {
        public MandatoryParamMissing(String param, String parentKey, ResponseCode responseCode) {
            super(responseCode.getErrorCode(),
                    MessageFormat.format(responseCode.getErrorMessage(), param), 400);
        }
        public MandatoryParamMissing(String param, String parentKey) {
            super(
                    IResponseMessage.Key.MANDATORY_PARAMETER_MISSING,
                    MessageFormat.format(
                            IResponseMessage.Message.MANDATORY_PARAMETER_MISSING, param),
                    400);
        }
    }

    public static class ParamDataTypeError extends BaseException {
        public ParamDataTypeError(String param, String type) {
            super(
                    IResponseMessage.INVALID_REQUESTED_DATA,
                    MessageFormat.format(
                           IResponseMessage.DATA_TYPE_ERROR, param),
                    400);
        }
    }

    public static class InvalidParamValue extends BaseException {
        public InvalidParamValue(String paramValue, String paramName) {
            super(
                    IResponseMessage.Key.INVALID_PARAMETER_VALUE,
                    MessageFormat.format(
                            ValidationException.getLocalizedMessage(
                                    IResponseMessage.INVALID_PARAMETER_VALUE, null),
                            paramValue,
                            paramName),
                    400);
        }
    }

    private static String getLocalizedMessage(String key, Locale locale) {
        return localizer.getMessage(key, locale);
    }
}
