/** */
package org.sunbird.pojo;

import java.io.Serializable;

/** @author manzarul */
public class Actor implements Serializable {

  /** */
  private static final long serialVersionUID = 6603396659308070730L;

  String id;
  String type;

  public Actor(String id, String type) {
    super();
    this.id = id;
    this.type = type;
  }

  public Actor() {
    super();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
