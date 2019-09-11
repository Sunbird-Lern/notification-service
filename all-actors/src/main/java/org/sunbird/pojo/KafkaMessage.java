/** */
package org.sunbird.pojo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** @author manzarul */
public class KafkaMessage implements Serializable {

  /** */
  private static final long serialVersionUID = -4424986739987941926L;

  private Actor actor;
  private String eid;
  private int iteration;
  private String mid;
  long ets;
  EventData edata;
  Context context;
  Map<String, Object> object = new HashMap<String, Object>();

  public Actor getActor() {
    return actor;
  }

  public void setActor(Actor actor) {
    this.actor = actor;
  }

  public String getEid() {
    return eid;
  }

  public void setEid(String eid) {
    this.eid = eid;
  }

  public int getIteration() {
    return iteration;
  }

  public void setIteration(int iteration) {
    this.iteration = iteration;
  }

  public String getMid() {
    return mid;
  }

  public void setMid(String mid) {
    this.mid = mid;
  }

  public long getEts() {
    return ets;
  }

  public void setEts(long ets) {
    this.ets = ets;
  }

  public EventData getEdata() {
    return edata;
  }

  public void setEdata(EventData edata) {
    this.edata = edata;
  }

  public Context getContext() {
    return context;
  }

  public void setContext(Context context) {
    this.context = context;
  }

  public Map<String, Object> getObject() {
    return object;
  }

  public void setObject(Map<String, Object> object) {
    this.object = object;
  }
}
