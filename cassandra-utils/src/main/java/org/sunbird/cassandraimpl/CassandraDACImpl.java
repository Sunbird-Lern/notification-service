package org.sunbird.cassandraimpl;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.sunbird.common.CassandraUtil;
import org.sunbird.common.Constants;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.request.LoggerUtil;
import org.sunbird.common.response.Response;

public class CassandraDACImpl extends CassandraOperationImpl {

  private static LoggerUtil logger = new LoggerUtil(CassandraDACImpl.class);

  public Response getRecords(
      String keySpace, String table, Map<String, Object> filters, List<String> fields, Map<String, Object> reqContext)
      throws BaseException {
    long startTime = System.currentTimeMillis();
    Response response = new Response();
    Select select= null;
    try{
      Session session = connectionManager.getSession(keySpace);
      if (CollectionUtils.isNotEmpty(fields)) {
        select = QueryBuilder.select((String[]) fields.toArray()).from(keySpace, table);
      } else {
        select = QueryBuilder.select().all().from(keySpace, table);
      }

      if (MapUtils.isNotEmpty(filters)) {
        Select.Where where = select.where();
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
          Object value = filter.getValue();
          if (value instanceof List) {
            where = where.and(QueryBuilder.in(filter.getKey(), ((List) filter.getValue())));
          } else {
            where = where.and(QueryBuilder.eq(filter.getKey(), filter.getValue()));
          }
        }
      }
      ResultSet results = null;
      results = session.execute(select);
      response = CassandraUtil.createResponse(results);
    } catch (Exception e) {
      logger.error(Constants.EXCEPTION_MSG_FETCH + table + " : " + e.getMessage(), e);
      throw new BaseException(
          e.getMessage(),
          IResponseMessage.SERVER_ERROR, ResponseCode.SERVER_ERROR.getCode());
    }finally {
      if (null != select) {
        logQueryElapseTime("getRecords", startTime, select.getQueryString(), reqContext);
      }
    }
    return response;
  }

  public void applyOperationOnRecordsAsync(
      String keySpace,
      String table,
      Map<String, Object> filters,
      List<String> fields,
      FutureCallback<ResultSet> callback,
      Map<String, Object> reqContext)
      throws BaseException {
    long startTime = System.currentTimeMillis();
    Session session = connectionManager.getSession(keySpace);
    Select select=null;
    try {
      if (CollectionUtils.isNotEmpty(fields)) {
        select = QueryBuilder.select((String[]) fields.toArray()).from(keySpace, table);
      } else {
        select = QueryBuilder.select().all().from(keySpace, table);
      }

      if (MapUtils.isNotEmpty(filters)) {
        Select.Where where = select.where();
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
          Object value = filter.getValue();
          if (value instanceof List) {
            where = where.and(QueryBuilder.in(filter.getKey(), ((List) filter.getValue())));
          } else {
            where = where.and(QueryBuilder.eq(filter.getKey(), filter.getValue()));
          }
        }
      }
      ResultSetFuture future = session.executeAsync(select);
      Futures.addCallback(future, callback, Executors.newFixedThreadPool(1));
    } catch (Exception e) {
      logger.error(Constants.EXCEPTION_MSG_FETCH + table + " : " + e.getMessage(), e);
      throw new BaseException(
          e.getMessage(),
          IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
    }finally {
      if (null != select) {
        logQueryElapseTime("applyOperationOnRecordsAsync", startTime, select.getQueryString(), reqContext);
      }
    }
  }

  public Response updateAddMapRecord(
      String keySpace,
      String table,
      Map<String, Object> primaryKey,
      String column,
      String key,
      Object value,
      Map<String, Object> reqContext)
      throws BaseException {
    return updateMapRecord(keySpace, table, primaryKey, column, key, value, true, reqContext);
  }

  public Response updateRemoveMapRecord(
      String keySpace, String table, Map<String, Object> primaryKey, String column, String key, Map<String, Object> reqContext)
      throws BaseException {
    return updateMapRecord(keySpace, table, primaryKey, column, key, null, false, reqContext);
  }

  public Response updateMapRecord(
      String keySpace,
      String table,
      Map<String, Object> primaryKey,
      String column,
      String key,
      Object value,
      boolean add,
      Map<String, Object> reqContext)
      throws BaseException {
    long startTime = System.currentTimeMillis();
    Update update = QueryBuilder.update(keySpace, table);
    if (add) {
      update.with(QueryBuilder.put(column, key, value));
    } else {
      update.with(QueryBuilder.remove(column, key));
    }
    try{
      if (MapUtils.isEmpty(primaryKey)) {
        String errorMsg = Constants.EXCEPTION_MSG_FETCH + table + " : primary key is a must for update call";
        logger.error(reqContext,errorMsg);
        throw new BaseException(
                errorMsg,
                IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
      }
      Update.Where where = update.where();
      for (Map.Entry<String, Object> filter : primaryKey.entrySet()) {
        Object filterValue = filter.getValue();
        if (filterValue instanceof List) {
          where = where.and(QueryBuilder.in(filter.getKey(), ((List) filter.getValue())));
        } else {
          where = where.and(QueryBuilder.eq(filter.getKey(), filter.getValue()));
        }
      }
      try {
        Response response = new Response();
        logger.debug( "Remove Map-Key Query: " + update.toString());
        connectionManager.getSession(keySpace).execute(update);
        response.put(Constants.RESPONSE, Constants.SUCCESS);
        return response;
      } catch (Exception e) {
        e.printStackTrace();
        logger.error(Constants.EXCEPTION_MSG_FETCH + table + " : " + e.getMessage(), e);
        throw new BaseException(
                e.getMessage(),
                IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
      }
    }finally {
      if (null != update) {
        logQueryElapseTime("updateMapRecord", startTime, update.getQueryString(), reqContext);
      }
    }
  }

  public Response updateAddSetRecord(
      String keySpace, String table, Map<String, Object> primaryKey, String column, Object value, Map<String, Object> reqContext)
      throws BaseException {
    return updateSetRecord(keySpace, table, primaryKey, column, value, true, reqContext);
  }

  public Response updateRemoveSetRecord(
      String keySpace, String table, Map<String, Object> primaryKey, String column, Object value, Map<String, Object> reqContext)
      throws BaseException {
    return updateSetRecord(keySpace, table, primaryKey, column, value, false, reqContext);
  }

  public Response updateSetRecord(
      String keySpace,
      String table,
      Map<String, Object> primaryKey,
      String column,
      Object value,
      boolean add,
      Map<String, Object> reqContext)
      throws BaseException {
    long startTime = System.currentTimeMillis();
    logger.debug("Cassandra Service updateSetRecord method started at == +"+ startTime);

    Update update = QueryBuilder.update(keySpace, table);
    if (add) {
      update.with(QueryBuilder.add(column, value));
    } else {
      update.with(QueryBuilder.remove(column, value));
    }
    try {
      if (MapUtils.isEmpty(primaryKey)) {
        String errorMsg = Constants.EXCEPTION_MSG_FETCH + table + " : primary key is a must for update call";
        logger.error(reqContext,errorMsg);
        throw new BaseException(
                errorMsg,
                IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
      }
      Update.Where where = update.where();
      for (Map.Entry<String, Object> filter : primaryKey.entrySet()) {
        Object filterValue = filter.getValue();
        if (filterValue instanceof List) {
          where = where.and(QueryBuilder.in(filter.getKey(), filterValue));
        } else {
          where = where.and(QueryBuilder.eq(filter.getKey(), filter.getValue()));
        }
      }
      Response response = new Response();
      try {
        logger.debug("updateSetRecord: Update set Query:: " + update.toString());
        connectionManager.getSession(keySpace).execute(update);
        response.put(Constants.RESPONSE, Constants.SUCCESS);
      } catch (Exception e) {
        logger.error(Constants.EXCEPTION_MSG_FETCH + table + " : " + e.getMessage(), e);
        throw new BaseException(
                e.getMessage(),
                IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
      }
      long stopTime = System.currentTimeMillis();
      logger.debug(
              MessageFormat.format("Cassandra operation {0} started at {1} and completed at {2}. Total time elapsed is {3}",
              "updateSetRecord",
              startTime,
              stopTime,
              (stopTime - startTime)));
      return response;
    }finally {
      if(null != update){
        logQueryElapseTime("updateSetRecord", startTime, update.getQueryString(), reqContext);
      }
    }
  }

}
