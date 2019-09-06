package org.sunbird.notification.fcm.provider;

import java.util.List;
import java.util.Map;

/**
 * This interface will handle all call regarding FCM notification
 * @author manzarul
 *
 */
public interface IFCMNotificationService {
	
	/**
	 * Method used for sending notification to single device.
	 * @param deviceId user device id.
	 * @param data notification data
	 * @return String it will have fcm notification response.
	 */
	public String sendSingleDeviceNotification(String deviceId, Map<String,String> data);
	
	/**
	 * This api will be used for sending notification to multiple device.
	 * max 100 device notification is supported here.
	 * @param deviceIds list of device ids
	 * @param data notification data
	 * @return List<String>
	 * @throws Exception 
	 */
	public List<String> sendMultiDeviceNotification (List<String> deviceIds , Map<String,String> data) throws Exception;
	

}
