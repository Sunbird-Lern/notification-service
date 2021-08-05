package org.sunbird;

/**
 * This interface will contains all the constants that's used throughout this application and shared
 * between User and Org module.
 *
 * @author Manzarul
 */
public interface JsonKey {

  String NOTIFICATION = "notification";
  String NOTIFICATIONS = "notifications";
  String DELIVERY_TYPE = "deliveryType";
  String MODE = "mode";
  String IDS = "ids";
  String MANDATORY_PARAMETER_MISSING = "MANDATORY_PARAMETER_MISSING";
  String INVALID_VALUE = "INVALID_VALUE";
  String VERIFY_OTP = "verifyOtp";

  String APP_ID = "appId";
  String DEVICE_ID = "did";
  String X_Session_ID = "x-session-id";
  String X_APP_VERSION = "x-app-ver";
  String X_TRACE_ENABLED = "x-trace-enabled";
  String X_REQUEST_ID = "x-request-id";
  String ACTOR_ID = "actorId";

  String TYPE = "type";
  String LEVEL = "level";
  String REQUEST_ID = "requestid";
  String MESSAGE = "message";
  String PARAMS = "params";
  String READ_FEED = "readFeed";
  String UPDATE_FEED = "updateFeed";
  String CREATE_NOTIFICATION = "createNotification";
}
