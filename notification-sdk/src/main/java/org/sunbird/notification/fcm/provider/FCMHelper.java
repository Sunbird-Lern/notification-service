/**
 * 
 */
package org.sunbird.notification.fcm.provider;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * This helper class will provide FirebaseMessaging instance.
 * @author manzarul
 *
 */
public class FCMHelper {

	private static FirebaseApp app = FirebaseApp.initializeApp();
	
	public static FirebaseMessaging getInstance () {
		return FirebaseMessaging.getInstance(app);
	}
	
}
