/** */
package org.sunbird.notification.dispatcher;

import org.sunbird.notification.utils.FCMResponse;
import org.sunbird.pojo.NotificationRequest;

/**
 * This interface is responsible for handling different mode of notification
 *
 * @author manzarul
 */
public interface INotificationDispatcher {

  public FCMResponse dispatch(NotificationRequest data, boolean isDryRun, boolean isSync);
}
