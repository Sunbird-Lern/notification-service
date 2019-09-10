/** */
package org.sunbird.notification.utils;

import java.util.List;

/** @author manzarul */
public class FCMResponse {
  long canonical_ids;
  int success;
  int failure;
  long multicast_id;
  int message_id;
  String error;
  List<Result> results;

  public long getCanonical_ids() {
    return canonical_ids;
  }

  public void setCanonical_ids(long canonical_ids) {
    this.canonical_ids = canonical_ids;
  }

  public int getSuccess() {
    return success;
  }

  public void setSuccess(int success) {
    this.success = success;
  }

  public int getFailure() {
    return failure;
  }

  public void setFailure(int failure) {
    this.failure = failure;
  }

  public long getMulticast_id() {
    return multicast_id;
  }

  public void setMulticast_id(long multicast_id) {
    this.multicast_id = multicast_id;
  }

  public List<Result> getResults() {
    return results;
  }

  public void setResults(List<Result> results) {
    this.results = results;
  }

  public int getMessage_id() {
    return message_id;
  }

  public void setMessage_id(int message_id) {
    this.message_id = message_id;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}
