package org.sunbird;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sunbird.message.IResponseMessage;
import org.sunbird.message.ResponseCode;
import org.sunbird.pojo.NotificationRequest;


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotificationRequestMapper {

    private static ObjectMapper mapper = new ObjectMapper();

    private static Logger logger = LogManager.getLogger(NotificationRequestMapper.class);

    public static List<NotificationRequest> toList(List<Map<String, Object>> request) throws BaseException {
        if (request.isEmpty()) {
            throw new BaseException("MANDATORY_PARAMETER_MISSING",
                    MessageFormat.format(IResponseMessage.MANDATORY_PARAMETER_MISSING, JsonKey.NOTIFICATIONS),
                    ResponseCode.CLIENT_ERROR.getCode());
        }
        List<NotificationRequest> sendNotificationList = new ArrayList<>();
        for (Map<String, Object> map : request) {
            sendNotificationList.add((getNotificationRequest(map)));
        }
        return sendNotificationList;
    }


    private static NotificationRequest getNotificationRequest(Map<String, Object> data) {
        NotificationRequest notificationRequest = mapper.convertValue(data, NotificationRequest.class);
        logger.info("Notification request , " + notificationRequest.toString());
        return notificationRequest;
    }
}
