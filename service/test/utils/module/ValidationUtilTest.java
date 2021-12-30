package utils.module;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.request.Request;
import org.sunbird.common.util.JsonKey;
import utils.ValidationUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationUtilTest {

    Map<String,Object> reqContext = new HashMap<>();
    @Test
    public void validateRequestObjectThrowValidationException() {
        Request request = createRequestObject();
        try {
            ValidationUtil.validateRequestObject(request);
        } catch (BaseException ex) {
            Assert.assertTrue(true);
            Assert.assertEquals("INVALID_REQUESTED_DATA", ex.getCode());
        }
    }

    @Test
    public void validateRequestObjectSuccess() {
        Request request = createRequestObject();
        request.put("name", "group1");
        try {
            ValidationUtil.validateRequestObject(request);
        } catch (BaseException ex) {
            Assert.assertTrue(false);
        }
        Assert.assertTrue(true);
    }

    /** Test ValidateMandatoryParamsWithType with String type parameter */
    @Test
    public void validateMandatoryParamsWithTypeString() {
        Request request = createRequestObject();
        request.put("name", "group1");
        try {
            ValidationUtil.validateMandatoryParamsWithType(
                    request.getRequest(), Lists.newArrayList("name"), String.class, true, JsonKey.REQUEST,reqContext);
        } catch (BaseException ex) {
            Assert.assertTrue(false);
        }
        Assert.assertTrue(true);
    }

    @Test
    public void validateMandatoryParamsWithTypeStringWithIntegerValue() {
        Request request = createRequestObject();
        request.put("name", 1);
        try {
            ValidationUtil.validateMandatoryParamsWithType(
                    request.getRequest(), Lists.newArrayList("name"), String.class, true, JsonKey.REQUEST,reqContext);
        } catch (BaseException ex) {
            Assert.assertTrue(true);
            Assert.assertEquals("DATA_TYPE_ERROR", ex.getMessage());
        }
    }

    @Test
    public void validateMandatoryParamsWithTypeStringWithEmptyValue() {
        Request request = createRequestObject();
        request.put("name", "");
        try {
            ValidationUtil.validateMandatoryParamsWithType(
                    request.getRequest(), Lists.newArrayList("name"), String.class, true, JsonKey.REQUEST,reqContext);
        } catch (BaseException ex) {
            Assert.assertTrue(true);
            Assert.assertEquals("Mandatory parameter name is missing", ex.getMessage());
        }
    }

    /** Test ValidateMandatoryParamsWithType with Integer type parameter */
    @Test
    public void validateMandatoryParamsWithTypeInteger() {
        Request request = createRequestObject();
        request.put("id", 1);
        try {
            ValidationUtil.validateMandatoryParamsWithType(
                    request.getRequest(), Lists.newArrayList("id"), Integer.class, true, JsonKey.REQUEST,reqContext);
        } catch (BaseException ex) {
            Assert.assertTrue(false);
        }
        Assert.assertTrue(true);
    }

    /** Test ValidateMandatoryParamsWithType with Map type parameter */
    @Test
    public void validateMandatoryParamsWithTypeMap() {
        Request request = createRequestObject();
        request.getRequest().put(JsonKey.FILTERS, new HashMap<>());
        try {
            ValidationUtil.validateMandatoryParamsWithType(
                    request.getRequest(),
                    Lists.newArrayList(JsonKey.FILTERS),
                    Map.class,
                    false,
                    JsonKey.REQUEST,reqContext);
        } catch (BaseException ex) {
            Assert.assertTrue(false);
        }
        Assert.assertTrue(true);
    }

    @Test
    public void validateMandatoryParamsWithTypeMapWithValue() {
        Request request = createRequestObject();
        Map<String, String> filterMap = new HashMap<>();
        filterMap.put("userId", "id1");
        request.getRequest().put(JsonKey.FILTERS, filterMap);
        try {
            ValidationUtil.validateMandatoryParamsWithType(
                    request.getRequest(),
                    Lists.newArrayList(JsonKey.FILTERS),
                    Map.class,
                    true,
                    JsonKey.REQUEST,reqContext);
        } catch (BaseException ex) {
            Assert.assertTrue(false);
        }
        Assert.assertTrue(true);
    }

    @Test
    public void validateMandatoryParamsWithTypeMapWithEmptyValueThrowsException() {
        Request request = createRequestObject();
        request.getRequest().put(JsonKey.FILTERS, new HashMap<>());
        try {
            ValidationUtil.validateMandatoryParamsWithType(
                    request.getRequest(),
                    Lists.newArrayList(JsonKey.FILTERS),
                    Map.class,
                    true,
                    JsonKey.REQUEST,reqContext);
        } catch (BaseException ex) {
            Assert.assertTrue(true);
            Assert.assertEquals("Mandatory parameter filters is missing", ex.getMessage());
        }
    }

    /** Test ValidateMandatoryParamsWithType with List type parameter */
    @Test
    public void validateMandatoryParamsWithTypeList() {
        Request request = createRequestObject();
        request.getRequest().put(JsonKey.FILTERS, new ArrayList<>());
        try {
            ValidationUtil.validateMandatoryParamsWithType(
                    request.getRequest(),
                    Lists.newArrayList(JsonKey.FILTERS),
                    List.class,
                    false,
                    JsonKey.REQUEST,reqContext);
        } catch (BaseException ex) {
            Assert.assertTrue(false);
        }
        Assert.assertTrue(true);
    }

    @Test
    public void validateMandatoryParamsWithTypeListWithValue() {
        Request request = createRequestObject();
        request.getRequest().put(JsonKey.FILTERS, Lists.newArrayList("group1", "group2"));
        try {
            ValidationUtil.validateMandatoryParamsWithType(
                    request.getRequest(),
                    Lists.newArrayList(JsonKey.FILTERS),
                    List.class,
                    true,
                    JsonKey.REQUEST,reqContext);
        } catch (BaseException ex) {
            Assert.assertTrue(false);
        }
        Assert.assertTrue(true);
    }

    @Test
    public void validateMandatoryParamsWithTypeListWithEmptyValueThrowsBaseException() {
        Request request = createRequestObject();
        request.getRequest().put(JsonKey.FILTERS, new ArrayList<>());
        try {
            ValidationUtil.validateMandatoryParamsWithType(
                    request.getRequest(),
                    Lists.newArrayList(JsonKey.FILTERS),
                    List.class,
                    true,
                    JsonKey.REQUEST,reqContext);
        } catch (BaseException ex) {
            Assert.assertTrue(true);
            Assert.assertEquals("Mandatory parameter filters is missing", ex.getMessage());
        }
    }

    @Test
    public void validateParamValue() {
        Map<String, List<String>> paramValue = new HashMap<>();
        paramValue.put(JsonKey.STATUS, Lists.newArrayList(JsonKey.ACTIVE, JsonKey.INACTIVE));
        paramValue.put(JsonKey.ROLE, Lists.newArrayList(JsonKey.ADMIN, JsonKey.MEMBER));
        Map<String, Object> member = new HashMap<>();
        member.put(JsonKey.STATUS, JsonKey.ACTIVE);
        member.put(JsonKey.ROLE, JsonKey.ADMIN);
        try {
            ValidationUtil.validateParamValue(
                    member, Lists.newArrayList(JsonKey.STATUS, JsonKey.ROLE), paramValue, JsonKey.MEMBERS,reqContext);
        } catch (BaseException ex) {
            Assert.assertTrue(false);
        }
        Assert.assertTrue(true);
    }

   

    @Test
    public void validateParamType() {
        Map<String,Object> request= new HashMap<>();
        request.put("name", "group1");
        request.put("members",new ArrayList<>());
        try {
            ValidationUtil.validateParamsWithType(request,Lists.newArrayList(JsonKey.MEMBERS,JsonKey.ACTIVITIES),
                    Map.class,JsonKey.REQUEST,reqContext);
            Assert.assertTrue(false);

        } catch (BaseException ex) {
            Assert.assertTrue(true);
        }
    }
    private Request createRequestObject() {
        Request request = new Request();
        Map<String, Object> map = new HashMap<>();
        request.setRequest(map);
        return request;
    }
}