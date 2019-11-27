package org.sunbird.notification.sms.providerimpl;

import org.sunbird.notification.beans.SMSConfig;
import org.sunbird.notification.sms.provider.ISmsProvider;
import org.sunbird.notification.sms.provider.ISmsProviderFactory;

public class Msg91SmsProviderFactory implements ISmsProviderFactory {

  private static Msg91SmsProviderImpl msg91SmsProvider = null;

  @Override
  public ISmsProvider create(SMSConfig config) {
    if (msg91SmsProvider == null) {
      msg91SmsProvider = new Msg91SmsProviderImpl(config.getAuthKey(), config.getSender());
    }
    return msg91SmsProvider;
  }
}
