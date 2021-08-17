package org.sunbird.utils;

import com.datastax.driver.core.Session;
import org.sunbird.common.exception.BaseException;

import java.util.List;

/**
 * Interface for cassandra connection manager , implementation would be Standalone and Embedde
 * cassandra connection manager .
 */
public interface CassandraConnectionManager {

  /**
   * Method to create the cassandra connection .
   *
   * @param hosts
   */
  void createConnection(String[] hosts) throws BaseException;

  /**
   * Method to get the cassandra session oject on basis of keyspace name provided .
   *
   * @param keyspaceName
   * @return Session
   */
  Session getSession(String keyspaceName);

  /**
   * Method to get the cassandra cluster oject on basis of keyspace name provided .
   *
   * @param keyspaceName
   * @return List<String>
   */
  List<String> getTableList(String keyspaceName);
}
