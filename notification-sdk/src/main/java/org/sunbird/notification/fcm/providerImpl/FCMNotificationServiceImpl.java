package org.sunbird.notification.fcm.providerImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.sunbird.notification.fcm.provider.FCMHelper;
import org.sunbird.notification.fcm.provider.IFCMNotificationService;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;

public class FCMNotificationServiceImpl implements IFCMNotificationService {
	private static Logger logger = Logger.getLogger("FCMNotificationServiceImpl");

	@Override
	public String sendSingleDeviceNotification(String deviceId, Map<String, String> data) {
		logger.info("sendSinfleDeviceNotification method started.");
		Message message = Message.builder().putAllData(data).setToken(deviceId).build();
		logger.info("Message going to be sent:" + message);
		String response = null;
		try {
			response = FCMHelper.getInstance().send(message);
			logger.info("Response from FCM :" + response);
		} catch (FirebaseMessagingException e) {
			logger.severe("Exception occured during notification sent: " + e.getMessage());
			e.printStackTrace();
		}
		return response;
	}

	@Override
	public List<String> sendMultiDeviceNotification(List<String> deviceIds, Map<String, String> data) throws Exception {
		List<String> responseDetails = new ArrayList<String>();
		if (deviceIds == null || deviceIds.size() == 0 || deviceIds.size() > 100) {
			throw new Exception("Either device id list is zero or greater than 100. Supported max size is 100.");
		}
		MulticastMessage message = MulticastMessage.builder().putAllData(data).addAllTokens(deviceIds).build();
		BatchResponse responses = FCMHelper.getInstance().sendMulticast(message);
		List<SendResponse> responseList = responses.getResponses();
		for (SendResponse response : responseList) {
			responseDetails.add(response.getMessageId());
		}
		return responseDetails;
	}
	
}
