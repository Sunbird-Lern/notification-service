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
  String TEMPLATE_ID = "templateId";
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
  String READ_V1_FEED = "readV1Feed";
  String UPDATE_FEED = "updateFeed";
  String UPDATE_V1_FEED = "updateV1Feed";
  String DELETE_FEED = "deleteFeed";
  String DELETE_V1_FEED = "deleteV1Feed";
  String CREATE_NOTIFICATION = "createNotification";
  String DOT_SEPARATOR = ".";
  String SHA_256_WITH_RSA = "SHA256withRSA";
  String UNAUTHORIZED = "Unauthorized";

  String PARENT_ID = "parentId";
  String SUB = "sub";
  String SUNBIRD_SSO_URL = "sunbird_sso_url";
  String SUNBIRD_SSO_REALM = "sunbird_sso_realm";
  String ACCESS_TOKEN_PUBLICKEY_BASEPATH = "accesstoken.publickey.basepath";
  String RESPONSE = "response";
  String ID = "id";
  String CREATED_BY = "createdBy";
  String ADDITIONAL_INFO = "additionalInfo";
  String DATA = "data";
  String VER = "ver";
  String TEMPLATE = "template";
  String USER_ID = "userId";
  String CATEGORY = "category";
  String ACTION = "action";
  String TEMPLATE_SCHEMA = "template_schema";
  String API_VERSION = "v1";
  String DEBUG_ENABLED = "debug-enabled";
  String SUNBIRD_NOTIFICATIONS = "sunbird_notifications";
  String SUNBIRD_CASSANDRA_IP = "sunbird_cassandra_host";
  String MANAGED_FOR = "managedFor";
  String STATUS = "status";
  String UPDATED_BY = "updatedBy";
  String UPDATED_ON = "updatedOn";
  String SUBJECT = "subject";
  String SENDER = "sender";
  String CONFIG = "config";
  String OTP = "otp";
  String TOPIC = "topic";
  String RAW_DATA = "rawData";
  String REQUEST_SOURCE = "source";
  String TELEMETRY_EVENT_TYPE = "telemetryEventType";

  String ERROR = "ERROR";
  String AUDIT = "ERROR";

  String CONTEXT = "context";
  String ERR = "err";
  String ERR_TYPE = "errType";
  String STACKTRACE = "stacktrace";
  String SYSTEM = "system";
  String VERSION = "version";
  String PRIORITY = "priority";
  String V1 = "v1";
  String ACTION_DATA = "actionData";
  String TITLE = "title";
  String DESCRIPTION = "description";
  String ACTION_TYPE = "actionType";
  String NOTIFICATION_CATEGORY_TYPE_CONFIG = "notification_category_type_config";
  String REQUEST = "request";
  String FEED_ID = "feedId";
  String VERSION_SUPPORT_CONFIG_ENABLE = "version_support_config_enable";
  String FEED_LIMIT = "feed_limit";
}
