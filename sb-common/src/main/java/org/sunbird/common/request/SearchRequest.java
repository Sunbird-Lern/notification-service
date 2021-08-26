package org.sunbird.common.request;

import java.util.HashMap;
import java.util.Map;

public class SearchRequest {
  private Map<String, Object> request = new HashMap<>();

  public Map<String, Object> getRequest() {
    return request;
  }

  public void setRequest(Map<String, Object> request) {
    this.request = request;
  }
}