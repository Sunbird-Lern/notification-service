package controllers.notification;

import static org.junit.Assert.*;

import java.util.Arrays;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sunbird.common.exception.BaseException;
import org.sunbird.JsonKey;
import org.sunbird.NotificationValidator;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.pojo.NotificationRequest;

public class NotificationValidatorTest {

  private NotificationRequest notificationRequest = new NotificationRequest();

  @Before
  public void setUp() throws Exception {
    notificationRequest.setMode("email");
    notificationRequest.setDeliveryType("message");
    notificationRequest.setIds(Arrays.asList("emailAddress", "phoneNumber", "deviceId"));
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public void validateSendNotificationRequestSuccess() {
    boolean response = false;
    try {
      NotificationValidator.validate(notificationRequest);
      response = true;
    } catch (BaseException e) {
      Assert.assertNull(e);
    }
    assertEquals(true, response);
  }

  @Test
  public void validateSendNotificationWithEmptyMode() {
    boolean response = false;
    notificationRequest.setMode("");
    try {
      NotificationValidator.validate(notificationRequest);
      response = true;
    } catch (BaseException e) {
      assertEquals(ResponseCode.BAD_REQUEST.getCode(), e.getResponseCode());
      assertEquals(JsonKey.MANDATORY_PARAMETER_MISSING, e.getCode());
    }
    assertEquals(false, response);
  }

  @Test
  public void validateSendNotificationWithInvalidModeValue() {
    boolean response = false;
    notificationRequest.setMode("gmail");
    try {
      NotificationValidator.validate(notificationRequest);
      response = true;
    } catch (BaseException e) {

      assertEquals(ResponseCode.BAD_REQUEST.getCode(), e.getResponseCode());
      assertEquals(JsonKey.INVALID_VALUE, e.getCode());
    }
    assertEquals(false, response);
  }

  @Test
  public void validateSendNotificationWithEmptyDeliveryType() {
    boolean response = false;
    notificationRequest.setDeliveryType("");
    try {
      NotificationValidator.validate(notificationRequest);
      response = true;
    } catch (BaseException e) {

      assertEquals(ResponseCode.BAD_REQUEST.getCode(), e.getResponseCode());
      assertEquals(JsonKey.MANDATORY_PARAMETER_MISSING, e.getCode());
    }
    assertEquals(false, response);
  }

  @Test
  public void validateSendNotificationWithEmptyIDs() {
    boolean response = false;
    notificationRequest.setIds(Arrays.asList());
    try {
      NotificationValidator.validate(notificationRequest);
      response = true;
    } catch (BaseException e) {
      assertEquals(ResponseCode.BAD_REQUEST.getCode(), e.getResponseCode());
      assertEquals(JsonKey.MANDATORY_PARAMETER_MISSING, e.getCode());
    }
    assertEquals(false, response);
  }
}
