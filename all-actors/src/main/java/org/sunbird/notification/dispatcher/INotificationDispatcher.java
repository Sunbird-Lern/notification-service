/** */
package org.sunbird.notification.dispatcher;

import java.util.List;
import java.util.Map;
import org.sunbird.notification.utils.FCMResponse;

/**
 * This interface is responsible for handling different mode of notification
 *
 * @author manzarul
 */
public interface INotificationDispatcher {

  public List<FCMResponse> dispatch(Map<String, Object> data, boolean isDryRun);

  public boolean dispatchAsync(Map<String, Object> data);
}
