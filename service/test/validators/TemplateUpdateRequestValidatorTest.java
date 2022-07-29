package validators;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.request.Request;
import org.sunbird.common.util.JsonKey;
import validators.TemplateUpdateRequestValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateUpdateRequestValidatorTest {


    @Test
    public void applySuccess() {
        Request request = createRequestObject();
        try {
            TemplateUpdateRequestValidator TemplateUpdateRequestValidator = new TemplateUpdateRequestValidator();
            TemplateUpdateRequestValidator.apply(request);
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
