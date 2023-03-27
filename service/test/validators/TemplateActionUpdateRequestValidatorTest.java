package validators;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.request.Request;
import org.sunbird.common.util.JsonKey;
import validators.TemplateActionUpdateRequestValidator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.response.Response;
import org.sunbird.request.LoggerUtil;
import utils.ValidationUtil;

public class TemplateActionUpdateRequestValidatorTest {

    @Test
    public void applySuccess() {
        Request request = createRequestObject();
        try {
            TemplateActionUpdateRequestValidator TemplateActionUpdateRequestValidator = new TemplateActionUpdateRequestValidator();
            TemplateActionUpdateRequestValidator.apply(request);
        } catch (BaseException ex) {
            Assert.assertTrue(true);
            Assert.assertEquals("INVALID_REQUESTED_DATA", ex.getCode());
        }
    }


    private Request createRequestObject() {
        Request request = new Request();
        Map<String, Object> map = new HashMap<>();
        request.setRequest(map);
        return request;
    }


}
