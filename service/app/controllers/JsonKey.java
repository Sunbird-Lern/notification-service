package controllers;


import java.util.Arrays;
import java.util.List;

/**
 * This interface will contains all the constants that's used throughout this application and shared
 * between User and Org module.
 *
 * @author Manzarul
 */
public interface JsonKey {

  String CLASS = "class";
  String DATA = "data";
  String EKS = "eks";
  String ID = "id";
  String LEVEL = "level";
  String MESSAGE = "message";
  String METHOD = "method";
  String REQUEST_MESSAGE_ID = "msgId";
  String STACKTRACE = "stacktrace";
  String VER = "ver";
  String OK = "ok";
  String LOG_LEVEL = "logLevel";
  String ERROR = "error";
  String EMPTY_STRING = "";
  String RESPONSE = "response";
  String ADDRESS = "address";
  String KEY = "key";
  String ERROR_MSG = "error_msg";
  String ATTRIBUTE = "attribute";
  String ERRORS = "errors";
  String SUCCESS = "success";
  String VERIFY_OTP = "verifyOtp";
  String URL = "url";
  String RESPONSE_CODE = "responseCode";
  String FAILED = "FAILED";
  String READ_FEED = "readFeed";
  String READ_V1_FEED ="readV1Feed";
  String UPDATE_FEED = "updateFeed";
  String UPDATE_V1_FEED = "updateV1Feed";
  String CREATE_NOTIFICATION = "createNotification";
  String DELETE_FEED = "deleteFeed";
  String DELETE_V1_FEED = "deleteV1Feed";

  String X_REQUEST_ID = "X-Request-ID";
  String USER_ID = "userId";
  String MANAGED_FOR = "managedFor";
  List<String> USER_UNAUTH_STATES = Arrays.asList(JsonKey.UNAUTHORIZED, JsonKey.ANONYMOUS);

  String UNAUTHORIZED = "Unauthorized";
  String ANONYMOUS = "Anonymous";
  String CONTEXT = "context";
  String NOTIFICATION = "notification";
  String ENV = "env";
  String REQUEST_TYPE = "requestType";
  String API_CALL = "API_CALL";
  String CHANNEL = "channel";
  String REQUEST_ID = "requestId";
  String APP_ID = "appId";
  String DEVICE_ID = "did";
  String X_TRACE_ENABLED = "x-trace-enabled";
  String ACTOR_ID = "actorId";
  String ACTOR_TYPE = "actorType";
  String USER = "user";
  String DEFAULT_CONSUMER_ID = "internal";
  String CONSUMER = "consumer";
  String PRIVATE = "private";
  String START_TIME = "startTime";
  String HASH_TAG_ID = "hashTagId";
  String VERSION = "version";
}
