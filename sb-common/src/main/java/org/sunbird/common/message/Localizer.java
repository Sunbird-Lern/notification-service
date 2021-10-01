package org.sunbird.common.message;

import java.util.Locale;
import java.util.ResourceBundle;

public class Localizer {

  ResourceBundle userResourceBundle = null;

  private static Localizer instance = null;

  private Localizer() {
    userResourceBundle = ResourceBundle.getBundle("responseMessages");
  }

  public static Localizer getInstance() {
    if (instance == null) instance = new Localizer();

    return instance;
  }

  public Locale getLocale(String baseLocale, String localeExtensions) {
    return new Locale(baseLocale, localeExtensions);
  }

  public String getMessage(String key, Locale locale) {

    if (null == locale) {
      return userResourceBundle.getString(key);
    } else {
      userResourceBundle = ResourceBundle.getBundle("responseMessages", locale);
      return userResourceBundle.getString(key);
    }
  }
}
