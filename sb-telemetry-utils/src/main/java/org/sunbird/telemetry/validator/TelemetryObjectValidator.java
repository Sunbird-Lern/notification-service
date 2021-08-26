package org.sunbird.telemetry.validator;

public interface TelemetryObjectValidator {

  public boolean validateAudit(String jsonString);

  public boolean validateSearch(String jsonString);

  public boolean validateLog(String jsonString);

  public boolean validateError(String jsonString);
}
