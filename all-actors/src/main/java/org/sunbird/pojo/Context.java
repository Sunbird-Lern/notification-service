/** */
package org.sunbird.pojo;

import java.io.Serializable;
import java.util.Map;

/** @author manzarul */
public class Context implements Serializable {

  /** */
  private static final long serialVersionUID = -5267883245767530083L;

  Map<String, Object> pdata;

  public Map<String, Object> getPdata() {
    return pdata;
  }

  public void setPdata(Map<String, Object> pdata) {
    this.pdata = pdata;
  }
}
