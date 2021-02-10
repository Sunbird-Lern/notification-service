/** */
package org.sunbird.pojo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.sunbird.util.Constant;

/** @author manzarul */
public class KafkaMessage implements Serializable {

  /** */
  private static final long serialVersionUID = -4424986739987941926L;

  private Actor actor;
  private String eid;

  private String mid;
  private Map<String, String> trace;
  long ets;
  EventData edata;
  Context context;
  Map<String, Object> object = new HashMap<String, Object>();

  public KafkaMessage() {
    this.ets = System.currentTimeMillis();
    this.eid = Constant.EID_VALUE;
    this.mid = Constant.PRODUCER_ID + "." + ets + "." + UUID.randomUUID();
    setContext();
  }

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

  private void setContext() {
    Context context = new Context();
    Map<String, Object> pdata = new HashMap<String, Object>();
    pdata.put(Constant.VER, Constant.VERSION_VALUE);
    pdata.put(Constant.ID, Constant.ID_VALUE);
    context.setPdata(pdata);
    this.context = context;
  }
  
  public Map<String, String> getTrace() {
    return trace;
  }
  
  public void setTrace(Map<String, String> trace) {
    this.trace = trace;
  }
}
