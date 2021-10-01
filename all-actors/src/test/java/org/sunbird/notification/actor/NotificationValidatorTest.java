package org.sunbird.notification.actor;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.JsonKey;
import org.sunbird.NotificationValidator;
import  org.sunbird.common.request.Request;
import org.sunbird.pojo.Config;
import org.sunbird.pojo.NotificationRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)

public class NotificationValidatorTest {
    @Test
    public void checkMandatoryParamWithStringTypeSuccess(){
        Map<String,Object> req = new HashMap<>();
        req.put("id","12323213");
        try{
            NotificationValidator.validateMandatoryParamsWithType(req, Arrays.asList("id"), String.class,
                    true,   "request",new HashMap<>());
            Assert.assertTrue(true);
        }catch (Exception ex){
            Assert.assertFalse(true);
        }
    }

    @Test
    public void checkMandatoryParamWithMapTypeSuccess(){
        Map<String,Object> req = new HashMap<>();
        Map<String,Object> val = new HashMap<>();
        val.put("type","user");
        req.put("id",val);
        Map<String,Object> reqObject = new HashMap<>();
        reqObject.put(JsonKey.REQUEST,req);
        try{
            NotificationValidator.validateMandatoryParamsWithType(req, Arrays.asList("id"), Map.class,
                    true,   "request",new HashMap<>());
            Assert.assertTrue(true);
        }catch (Exception ex){
            Assert.assertFalse(true);
        }
    }

    @Test
    public void checkMandatoryParamWithListSuccess(){
        Map<String,Object> req = new HashMap<>();

        req.put("id",Arrays.asList("123231232"));
        Map<String,Object> reqObject = new HashMap<>();
        reqObject.put(JsonKey.REQUEST,req);
        try{
            NotificationValidator.validateMandatoryParamsWithType(req, Arrays.asList("id"), List.class,
                    true,   "request",new HashMap<>());
            Assert.assertTrue(true);
        }catch (Exception ex){
            Assert.assertFalse(true);
        }
    }

    @Test
    public void checkMandatoryParamWithStringTypeFailed(){
        Map<String,Object> req = new HashMap<>();
        req.put("id","");
        try{
            NotificationValidator.validateMandatoryParamsWithType(req, Arrays.asList("id"), String.class,
                    true,   "request",new HashMap<>());
            Assert.assertFalse(true);
        }catch (Exception ex){
            Assert.assertTrue(true);
        }
    }

    @Test
    public void checkMandatoryParamWithMapTypeFailed(){
        Map<String,Object> req = new HashMap<>();
        Map<String,Object> val = new HashMap<>();

        req.put("id",val);
        Map<String,Object> reqObject = new HashMap<>();
        reqObject.put(JsonKey.REQUEST,req);
        try{
            NotificationValidator.validateMandatoryParamsWithType(req, Arrays.asList("id"), Map.class,
                    true,   "request",new HashMap<>());
            Assert.assertFalse(true);
        }catch (Exception ex){
            Assert.assertTrue(true);
        }
    }

    @Test
    public void checkMandatoryParamWithListFailed(){
        Map<String,Object> req = new HashMap<>();

        req.put("id",Arrays.asList());
        Map<String,Object> reqObject = new HashMap<>();
        reqObject.put(JsonKey.REQUEST,req);
        try{
            NotificationValidator.validateMandatoryParamsWithType(req, Arrays.asList("id"), List.class,
                    true,   "request",new HashMap<>());
            Assert.assertFalse(true);

        }catch (Exception ex){
            Assert.assertTrue(true);
        }
    }

    @Test
    public void validateDeleteRequest(){
        Map<String,Object> req = new HashMap<>();

        req.put("id",Arrays.asList("12121"));
        req.put("userId","21321321");
        req.put("category","groups");
        Request request = new Request();
        request.put(JsonKey.REQUEST,req);
        try{
            NotificationValidator.validateDeleteRequest(request);
            Assert.assertFalse(true);

        }catch (Exception ex){
            Assert.assertTrue(true);
        }
    }

    @Test
    public void validatePramWithTypeRequest(){
        Map<String,Object> req = new HashMap<>();

        req.put("id",Arrays.asList("12121"));
        req.put("userId","21321321");
        req.put("category","groups");
        Request request = new Request();
        request.put(JsonKey.REQUEST,req);
        try{
            NotificationValidator.validateParamsWithType(request.getRequest(), Lists.newArrayList(JsonKey.IDS),
                    List.class,JsonKey.REQUEST,request.getContext());
            Assert.assertTrue(true);

        }catch (Exception ex){
            Assert.assertFalse( true);

        }
    }

    @Test
    public void validate(){
        NotificationRequest request = new NotificationRequest();
        request.setIds(Arrays.asList("12121"));
        request.setDeliveryType("email");
        request.setConfig(new Config());
        request.setMode("email");
        try{
            NotificationValidator.validate(request);
            Assert.assertTrue(true);

        }catch (Exception ex){
            Assert.assertFalse(true);
        }
    }
}
