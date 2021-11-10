package org.sunbird.common.message;

/**
 * This interface will hold all the response key and message
 *
 * @author Amit Kumar
 */
public interface IResponseMessage extends IUserResponseMessage, IOrgResponseMessage {

  String INVALID_OPERATION_NAME = "INVALID_OPERATION_NAME";
  String INTERNAL_ERROR = "INTERNAL_ERROR";
  String SERVER_ERROR = "SERVER_ERROR";
  String INVALID_VALUE = "{0} VALUE IS INVALID, {1}";
  String MAX_NOTIFICATION_SIZE = "Max supported id in single playload is {0}";
  String SERVICE_UNAVAILABLE = "SERVICE UNAVAILABLE";
  String INVALID_PARAMETER_VALUE = "INVALID_PARAMETER_VALUE";
  String DATA_TYPE_ERROR = "DATA_TYPE_ERROR";

  interface Key{

    String MANDATORY_PARAMETER_MISSING = "MANDATORY_PARAMETER_MISSING";
    String TEMPLATE_NOT_FOUND = "TEMPLATE_NOT_FOUND";
    String INVALID_REQUESTED_DATA = "INVALID_REQUESTED_DATA";
    String UNAUTHORIZED = "UNAUTHORIZED";
    String SERVER_ERROR = "INTERNAL_ERROR";
    String INVALID_PARAMETER_VALUE = "INVALID_PARAMETER_VALUE";
  }

  interface Message {

    String MANDATORY_PARAMETER_MISSING = "Mandatory parameter {0} is missing";
    String TEMPLATE_NOT_FOUND = "Template is not pre configured for {0} type";
    String UNAUTHORIZED = "you are an unauthorized user";
    String INVALID_REQUESTED_DATA = "Invalid request data is passed";
    String INTERNAL_ERROR = "INTERNAL_ERROR";

  }
}