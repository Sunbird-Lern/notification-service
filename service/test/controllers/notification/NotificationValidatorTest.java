package controllers.notification;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sunbird.BaseException;
import org.sunbird.JsonKey;
import org.sunbird.message.ResponseCode;
import org.sunbird.NotificationValidator;
import org.sunbird.request.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class NotificationValidatorTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void validateSendNotificationRequestSuccess() {
        Request request = new Request();
        boolean response = false;
        Map<String, Object> requestObj = new HashMap<>();
        List<Map<String, Object>> sendReq = new ArrayList<>();

        Map<String, Object> notification = new HashMap<>();
        notification.put(JsonKey.MODE, "email");
        notification.put(JsonKey.DELIVERY_TYPE, "message");
        notification.put(JsonKey.IDS, new String[]{"emailAddress", "phoneNumber", "deviceId"});

        sendReq.add(notification);
        requestObj.put(JsonKey.NOTIFICATIONS, sendReq);
        request.setRequest(requestObj);
        try {
            NotificationValidator.validateSendNotificationRequest(request);
            response = true;
        } catch (BaseException e) {
            Assert.assertNull(e);
        }
        assertEquals(true, response);
    }


    @Test
    public void validateSendNotificationWithEmptyMode() {
        Request request = new Request();
        boolean response = false;
        Map<String, Object> requestObj = new HashMap<>();
        List<Map<String, Object>> sendReq = new ArrayList<>();

        Map<String, Object> notification = new HashMap<>();
        notification.put(JsonKey.MODE, "");
        notification.put(JsonKey.DELIVERY_TYPE, "message");
        notification.put(JsonKey.IDS, new String[]{"emailAddress", "phoneNumber", "deviceId"});

        sendReq.add(notification);
        requestObj.put(JsonKey.NOTIFICATIONS, sendReq);
        request.setRequest(requestObj);
        try {
            NotificationValidator.validateSendNotificationRequest(request);
            response = true;
        } catch (BaseException e) {
            assertEquals(ResponseCode.BAD_REQUEST.getCode(), e.getResponseCode());
            assertEquals(JsonKey.MANDATORY_PARAMETER_MISSING, e.getCode());
        }
        assertEquals(false, response);
    }

    @Test
    public void validateSendNotificationWithInvalidModeValue()  {
        Request request = new Request();
        boolean response = false;
        Map<String, Object> requestObj = new HashMap<>();
        List<Map<String, Object>> sendReq = new ArrayList<>();

        Map<String, Object> notification = new HashMap<>();
        notification.put(JsonKey.MODE, "gmail");
        notification.put(JsonKey.DELIVERY_TYPE, "message");
        notification.put(JsonKey.IDS, new String[]{"emailAddress", "phoneNumber", "deviceId"});

        sendReq.add(notification);
        requestObj.put(JsonKey.NOTIFICATIONS, sendReq);
        request.setRequest(requestObj);
        try {
            NotificationValidator.validateSendNotificationRequest(request);
            response = true;
        } catch (BaseException e) {

            assertEquals(ResponseCode.BAD_REQUEST.getCode(), e.getResponseCode());
            assertEquals(JsonKey.INVALID_VALUE, e.getCode());
        }
        assertEquals(false, response);
    }

    @Test
    public void  validateSendNotificationWithEmptyDeliveryType()    {
        Request request = new Request();
        boolean response = false;
        Map<String, Object> requestObj = new HashMap<>();
        List<Map<String, Object>> sendReq = new ArrayList<>();

        Map<String, Object> notification = new HashMap<>();
        notification.put(JsonKey.MODE, "email");
        notification.put(JsonKey.DELIVERY_TYPE, "");
        notification.put(JsonKey.IDS, new String[]{"emailAddress", "phoneNumber", "deviceId"});

        sendReq.add(notification);
        requestObj.put(JsonKey.NOTIFICATIONS, sendReq);
        request.setRequest(requestObj);
        try {
            NotificationValidator.validateSendNotificationRequest(request);
            response = true;
        } catch (BaseException e) {

            assertEquals(ResponseCode.BAD_REQUEST.getCode(), e.getResponseCode());
            assertEquals(JsonKey.MANDATORY_PARAMETER_MISSING, e.getCode());
        }
        assertEquals(false, response);
    }

    @Test
    public void  validateSendNotificationWithEmptyIDs()    {
        Request request = new Request();
        boolean response = false;
        Map<String, Object> requestObj = new HashMap<>();
        List<Map<String, Object>> sendReq = new ArrayList<>();

        Map<String, Object> notification = new HashMap<>();
        notification.put(JsonKey.MODE, "email");
        notification.put(JsonKey.DELIVERY_TYPE, "");
        notification.put(JsonKey.IDS, new String[]{});

        sendReq.add(notification);
        requestObj.put(JsonKey.NOTIFICATIONS, sendReq);
        request.setRequest(requestObj);
        try {
            NotificationValidator.validateSendNotificationRequest(request);
            response = true;
        } catch (BaseException e) {
            assertEquals(ResponseCode.BAD_REQUEST.getCode(), e.getResponseCode());
            assertEquals(JsonKey.MANDATORY_PARAMETER_MISSING, e.getCode());
        }
        assertEquals(false, response);
    }
}


