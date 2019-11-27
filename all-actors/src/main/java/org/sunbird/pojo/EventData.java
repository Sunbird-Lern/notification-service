/** */
package org.sunbird.pojo;

import org.sunbird.util.Constant;

import java.io.Serializable;
import java.util.Map;

/** @author manzarul */
public class EventData implements Serializable {

  /** */
  private static final long serialVersionUID = 3676213939777740836L;

  String action;
  private int iteration;
  Map<String, Object> request;

  public EventData() {
    this.iteration = Constant.NUMBER_OF_ITERATION;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public Map<String, Object> getRequest() {
    return request;
  }

  public void setRequest(Map<String, Object> request) {
    this.request = request;
  }

  public int getIteration() {
    return iteration;
  }

  public void setIteration(int iteration) {
    this.iteration = iteration;
  }
}
