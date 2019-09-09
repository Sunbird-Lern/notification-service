package org.sunbird.pojo;

import java.util.ArrayList;
import java.util.List;

/** Mode in which the notification is sent */
public class NotificationMode {

  private static List<String> modeType = new ArrayList<>();

  static {
    for (NotificationModeType mode : NotificationModeType.values()) {
      modeType.add(mode.toString());
    }
  }

  public static List<String> get() {
    return modeType;
  }

  public enum NotificationModeType {
    phone,
    email,
    device
  }
}
