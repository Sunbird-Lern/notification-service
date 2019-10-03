package org.sunbird.util.validator;

import org.apache.commons.lang3.StringUtils;

public class OtpRequestValidator {

  public static boolean isOtpVerifyRequestValid(String key, String otp) {
    boolean response = true;
    if (StringUtils.isBlank(key)) {
      response = false;
    }
    if (!StringUtils.isNumeric(otp)) {
      response = false;
    }

    return response;
  }
}
