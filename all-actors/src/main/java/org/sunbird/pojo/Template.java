package org.sunbird.pojo;

import com.fasterxml.jackson.databind.JsonNode;

public class Template {

  private String id;

  private String data;

  private JsonNode params;

  public Template() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public JsonNode getParams() {
    return params;
  }

  public void setParams(JsonNode params) {
    this.params = params;
  }
}
