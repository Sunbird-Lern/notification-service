/** */
package org.sunbird.notification.dispatcher;

import java.util.Map;

/**
 * This interface is responsible for handling different mode of notification
 *
 * @author manzarul
 */
public interface INotificationDispatcher {

  public void dispatch(Map<String, Object> data, boolean isDryRun);
}
