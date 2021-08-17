package org.sunbird.utils;

public class CassandraConnectionMngrFactory {

  private static CassandraConnectionManager instance;

  public static CassandraConnectionManager getInstance() {
    if (instance == null) {
      synchronized (CassandraConnectionMngrFactory.class) {
        if (instance == null) {
          instance = new CassandraConnectionManagerImpl();
        }
      }
    }
    return instance;
  }
}
