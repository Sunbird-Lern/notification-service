package org.sunbird.notification.sms.provider;

import org.sunbird.notification.beans.SMSConfig;

public interface ISmsProviderFactory {

  ISmsProvider create(SMSConfig config);
}
