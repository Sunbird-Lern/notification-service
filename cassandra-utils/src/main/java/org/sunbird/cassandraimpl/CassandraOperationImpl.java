package org.sunbird.cassandraimpl;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.QueryValidationException;
import com.datastax.driver.core.querybuilder.*;
import com.datastax.driver.core.querybuilder.Select.Builder;
import com.datastax.driver.core.querybuilder.Select.Selection;
import com.datastax.driver.core.querybuilder.Select.Where;
import com.datastax.driver.core.querybuilder.Update.Assignments;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.concurrent.FutureCallback;
import org.sunbird.cassandra.CassandraOperation;
import org.sunbird.common.CassandraUtil;
import org.sunbird.common.Constants;
import org.sunbird.common.exception.BaseException;
import org.sunbird.utils.CassandraConnectionManager;
import org.sunbird.utils.CassandraConnectionMngrFactory;
import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.Localizer;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.request.LoggerUtil;
import org.sunbird.common.response.Response;

/**
 * @author Amit Kumar
 * @desc this class will hold functions for cassandra db interaction
 */
public abstract class CassandraOperationImpl implements CassandraOperation {

  protected CassandraConnectionManager connectionManager;

  private static LoggerUtil logger = new LoggerUtil(CassandraOperationImpl.class);

  protected Localizer localizer = Localizer.getInstance();

  public CassandraOperationImpl() {
    connectionManager = CassandraConnectionMngrFactory.getInstance();
  }

  @Override
  public Response insertRecord(String keyspaceName, String tableName, Map<String, Object> request, Map<String, Object> reqContext)
      throws BaseException {
    long startTime = System.currentTimeMillis();
    logger.debug(reqContext,MessageFormat.format("Cassandra Service insertRecord method started at == {0}", startTime));
    Response response = new Response();
    String query=null;
    try {
      query = CassandraUtil.getPreparedStatement(keyspaceName, tableName, request);
      PreparedStatement statement = connectionManager.getSession(keyspaceName).prepare(query);
      BoundStatement boundStatement = new BoundStatement(statement);
      Iterator<Object> iterator = request.values().iterator();
      Object[] array = new Object[request.keySet().size()];
      int i = 0;
      while (iterator.hasNext()) {
        array[i++] = iterator.next();
      }
      connectionManager.getSession(keyspaceName).execute(boundStatement.bind(array));
      response.put(Constants.RESPONSE, Constants.SUCCESS);
    } catch (Exception e) {
      if (e.getMessage().contains(Constants.UNKNOWN_IDENTIFIER)
          || e.getMessage().contains(Constants.UNDEFINED_IDENTIFIER)) {
        logger.error(reqContext,
            "Exception occured while inserting record to " + tableName + " : " + e.getMessage());
        throw new BaseException(
                CassandraUtil.processExceptionForUnknownIdentifier(e),
                IResponseMessage.SERVER_ERROR, ResponseCode.SERVER_ERROR.getCode()
            );
      }
      logger.error(reqContext, "Exception occured while inserting record to " + tableName + " : " + e.getMessage());
      throw new BaseException(
              e.getMessage(),
              IResponseMessage.SERVER_ERROR, ResponseCode.SERVER_ERROR.getCode());
    }finally {
      if(null != query){
        logQueryElapseTime("insertRecord", startTime,query,reqContext);
      }
    }
    return response;
  }

  @Override
  public Response updateRecord(String keyspaceName, String tableName, Map<String, Object> request, Map<String, Object> reqContext)
      throws BaseException {
    long startTime = System.currentTimeMillis();
    logger.debug(reqContext, MessageFormat.format("Cassandra Service updateRecord method started at == {0}", startTime));
    Response response = new Response();
    String query = null;
    try {
      query = CassandraUtil.getUpdateQueryStatement(keyspaceName, tableName, request);
      PreparedStatement statement = connectionManager.getSession(keyspaceName).prepare(query);
      Object[] array = new Object[request.size()];
      int i = 0;
      String str = "";
      int index = query.lastIndexOf(Constants.SET.trim());
      str = query.substring(index + 4);
      str = str.replace(Constants.EQUAL_WITH_QUE_MARK, "");
      str = str.replace(Constants.WHERE_ID, "");
      str = str.replace(Constants.SEMICOLON, "");
      String[] arr = str.split(",");
      for (String key : arr) {
        array[i++] = request.get(key.trim());
      }
      array[i] = request.get(Constants.IDENTIFIER);
      BoundStatement boundStatement = statement.bind(array);
      connectionManager.getSession(keyspaceName).execute(boundStatement);
      response.put(Constants.RESPONSE, Constants.SUCCESS);
    } catch (Exception e) {
      if (e.getMessage().contains(Constants.UNKNOWN_IDENTIFIER)) {
        logger.error(reqContext, Constants.EXCEPTION_MSG_UPDATE + tableName + " : " + e.getMessage());
        throw new BaseException(
            localizer.getMessage(CassandraUtil.processExceptionForUnknownIdentifier(e), null),
                IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
      }
      logger.error(reqContext, Constants.EXCEPTION_MSG_UPDATE + tableName + " : " + e.getMessage());
      throw new BaseException(
          localizer.getMessage(IResponseMessage.SERVER_ERROR, null),
              IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
    }finally {
      if(null != query){
        logQueryElapseTime("updateRecord", startTime,query,reqContext);
      }
    }
    return response;
  }

  @Override
  public Response deleteRecord(String keyspaceName, String tableName, String identifier, Map<String, Object> reqContext)
      throws BaseException {
    long startTime = System.currentTimeMillis();
    logger.debug(reqContext, MessageFormat.format("Cassandra Service deleteRecord method started at == {0}", startTime));
    Response response = new Response();
    Delete.Where delete = null;
    try {
       delete =
          QueryBuilder.delete()
              .from(keyspaceName, tableName)
              .where(eq(Constants.IDENTIFIER, identifier));
      connectionManager.getSession(keyspaceName).execute(delete);
      response.put(Constants.RESPONSE, Constants.SUCCESS);
    } catch (Exception e) {
      logger.error(reqContext, Constants.EXCEPTION_MSG_DELETE + tableName + " : " + e.getMessage());
      throw new BaseException(
              e.getMessage(),
          IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
    }finally {
      if(null != delete) {
        logQueryElapseTime("deleteRecord", startTime,delete.getQueryString(),reqContext);
      }
    }
    return response;
  }

  @Override
  public Response getRecordsByProperty(
      String keyspaceName, String tableName, String propertyName, Object propertyValue, Map<String, Object> reqContext)
      throws BaseException {
    return getRecordsByProperty(keyspaceName, tableName, propertyName, propertyValue, null, reqContext);
  }

  @Override
  public Response getRecordsByProperty(
      String keyspaceName,
      String tableName,
      String propertyName,
      Object propertyValue,
      List<String> fields,
      Map<String, Object> reqContext)
      throws BaseException {
    long startTime = System.currentTimeMillis();
    Response response = new Response();
    Session session = connectionManager.getSession(keyspaceName);
    Builder selectBuilder=null;
    try {
      if (CollectionUtils.isNotEmpty(fields)) {
        selectBuilder = QueryBuilder.select((String[]) fields.toArray());
      } else {
        selectBuilder = QueryBuilder.select().all();
      }
      Statement selectStatement =
          selectBuilder.from(keyspaceName, tableName).where(eq(propertyName, propertyValue));
      ResultSet results = null;
      results = session.execute(selectStatement);
      response = CassandraUtil.createResponse(results);
    } catch (Exception e) {
      logger.error(reqContext, Constants.EXCEPTION_MSG_FETCH + tableName + " : " + e.getMessage());
      throw new BaseException(
              e.getMessage(),
          IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
    }finally {
      if(null != selectBuilder){
        logQueryElapseTime("getRecordsByProperty", startTime,selectBuilder.toString(),reqContext);
      }
    }
    return response;
  }

  @Override
  public Response getRecordsByProperty(
      String keyspaceName, String tableName, String propertyName, List<Object> propertyValueList, Map<String, Object> reqContext)
      throws BaseException {
    return getRecordsByProperty(keyspaceName, tableName, propertyName, propertyValueList, null,reqContext);
  }

  @Override
  public Response getRecordsByProperty(
      String keyspaceName,
      String tableName,
      String propertyName,
      List<Object> propertyValueList,
      List<String> fields,
      Map<String, Object> reqContext)
      throws BaseException {
    long startTime = System.currentTimeMillis();
    logger.debug(reqContext, MessageFormat.format("Cassandra Service getRecordsByProperty method started at == {0}", startTime));
    Response response = new Response();
    Builder selectBuilder= null;
    try {
      if (CollectionUtils.isNotEmpty(fields)) {
        selectBuilder = QueryBuilder.select(fields.toArray(new String[fields.size()]));
      } else {
        selectBuilder = QueryBuilder.select().all();
      }
      Statement selectStatement =
          selectBuilder
              .from(keyspaceName, tableName)
              .where(QueryBuilder.in(propertyName, propertyValueList));
      ResultSet results = connectionManager.getSession(keyspaceName).execute(selectStatement);
      response = CassandraUtil.createResponse(results);
    } catch (Exception e) {
      logger.error(reqContext, Constants.EXCEPTION_MSG_FETCH + tableName + " : " + e.getMessage());
      throw new BaseException(
              e.getMessage(),
          IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
    }finally {
      if(null != selectBuilder){
        logQueryElapseTime("getRecordsByProperty", startTime,selectBuilder.toString(),reqContext);
      }
    }
    return response;
  }

  @Override
  public Response getRecordsByProperties(
      String keyspaceName, String tableName, Map<String, Object> propertyMap, Map<String, Object> reqContext) throws BaseException {
    return getRecordsByProperties(keyspaceName, tableName, propertyMap, null,reqContext);
  }

  @Override
  public Response getRecordsByProperties(
      String keyspaceName, String tableName, Map<String, Object> propertyMap, List<String> fields,Map<String, Object> reqContext)
      throws BaseException {
    long startTime = System.currentTimeMillis();
    logger.debug(reqContext, MessageFormat.format("Cassandra Service getRecordsByProperties method started at == {0}", startTime));
    Response response = new Response();
    Builder selectBuilder=null;
    try {
      if (CollectionUtils.isNotEmpty(fields)) {
        String[] dbFields = fields.toArray(new String[fields.size()]);
        selectBuilder = QueryBuilder.select(dbFields);
      } else {
        selectBuilder = QueryBuilder.select().all();
      }
      Select selectQuery = selectBuilder.from(keyspaceName, tableName);
      if (MapUtils.isNotEmpty(propertyMap)) {
        Where selectWhere = selectQuery.where();
        for (Entry<String, Object> entry : propertyMap.entrySet()) {
          if (entry.getValue() instanceof List) {
            List<Object> list = (List) entry.getValue();
            if (null != list) {
              Object[] propertyValues = list.toArray(new Object[list.size()]);
              Clause clause = QueryBuilder.in(entry.getKey(), propertyValues);
              selectWhere.and(clause);
            }
          } else {
            Clause clause = eq(entry.getKey(), entry.getValue());
            selectWhere.and(clause);
          }
        }
      }
      // TODO : selectQuery.allowFiltering() is removed for now. Need to add a separate method for
      // it
      ResultSet results = connectionManager.getSession(keyspaceName).execute(selectQuery);
      response = CassandraUtil.createResponse(results);
    } catch (Exception e) {
      logger.error(reqContext,Constants.EXCEPTION_MSG_FETCH + tableName + " : " + e.getMessage());
      throw new BaseException(
              e.getMessage(),
          IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
    }finally {
      if (null != selectBuilder) {
        logQueryElapseTime("getRecordsByProperties", startTime,selectBuilder.toString(),reqContext);
      }
    }
    return response;
  }

  @Override
  public Response getPropertiesValueById(
      String keyspaceName, String tableName, String id, Map<String, Object> reqContext, String... properties) throws BaseException {
    long startTime = System.currentTimeMillis();
    logger.debug(reqContext, MessageFormat.format("Cassandra Service getPropertiesValueById method started at == {0}", startTime));
    Response response = new Response();
    String selectQuery = CassandraUtil.getSelectStatement(keyspaceName, tableName, properties);
    try {
      PreparedStatement statement = connectionManager.getSession(keyspaceName).prepare(selectQuery);
      BoundStatement boundStatement = new BoundStatement(statement);
      ResultSet results =
          connectionManager.getSession(keyspaceName).execute(boundStatement.bind(id));
      response = CassandraUtil.createResponse(results);
    } catch (Exception e) {
      logger.error(Constants.EXCEPTION_MSG_FETCH + tableName + " : " + e.getMessage(), e);
      throw new BaseException(
              e.getMessage(),
          IResponseMessage.SERVER_ERROR, ResponseCode.SERVER_ERROR.getCode());
    }finally {
      logQueryElapseTime("getPropertiesValueById", startTime,selectQuery,reqContext);
    }
    return response;
  }

  @Override
  public Response getAllRecords(String keyspaceName, String tableName, Map<String, Object> reqContext) throws BaseException {
    long startTime = System.currentTimeMillis();
    logger.debug(reqContext, MessageFormat.format("Cassandra Service getAllRecords method started at == {0}", startTime));
    Response response = new Response();
    Select selectQuery = QueryBuilder.select().all().from(keyspaceName, tableName);
    try {
      ResultSet results = connectionManager.getSession(keyspaceName).execute(selectQuery);
      response = CassandraUtil.createResponse(results);
    } catch (Exception e) {
      logger.error(reqContext, Constants.EXCEPTION_MSG_FETCH + tableName + " : " + e.getMessage());
      throw new BaseException(
              e.getMessage(),
          IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
    }finally {
      logQueryElapseTime("getAllRecords", startTime,selectQuery.getQueryString(),reqContext);
    }
    return response;
  }

  @Override
  public Response upsertRecord(String keyspaceName, String tableName, Map<String, Object> request, Map<String, Object> reqContext)
      throws BaseException {
    long startTime = System.currentTimeMillis();
    logger.debug(reqContext, MessageFormat.format("Cassandra Service upsertRecord method started at == {0}", startTime));
    Response response = new Response();
    String query = CassandraUtil.getPreparedStatement(keyspaceName, tableName, request);
    try {
      PreparedStatement statement = connectionManager.getSession(keyspaceName).prepare(query);
      BoundStatement boundStatement = new BoundStatement(statement);
      Iterator<Object> iterator = request.values().iterator();
      Object[] array = new Object[request.keySet().size()];
      int i = 0;
      while (iterator.hasNext()) {
        array[i++] = iterator.next();
      }
      connectionManager.getSession(keyspaceName).execute(boundStatement.bind(array));
      response.put(Constants.RESPONSE, Constants.SUCCESS);

    } catch (Exception e) {
      if (e.getMessage().contains(Constants.UNKNOWN_IDENTIFIER)) {
        logger.error(reqContext, Constants.EXCEPTION_MSG_UPSERT + tableName + " : " + e.getMessage());
        throw new BaseException(
            IResponseMessage.SERVER_ERROR,
            localizer.getMessage(CassandraUtil.processExceptionForUnknownIdentifier(e), null),
            ResponseCode.CLIENT_ERROR.getCode());
      }
      logger.error(Constants.EXCEPTION_MSG_UPSERT + tableName + " : " + e.getMessage(), e);
      throw new BaseException(
              e.getMessage(),
          IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
    }finally {
      logQueryElapseTime("upsertRecord", startTime,query,reqContext);
    }
    return response;
  }

  @Override
  public Response updateRecord(
      String keyspaceName,
      String tableName,
      Map<String, Object> request,
      Map<String, Object> compositeKey,
      Map<String, Object> reqContext)
      throws BaseException {

    long startTime = System.currentTimeMillis();
    logger.debug(reqContext, MessageFormat.format("Cassandra Service updateRecord method started at == {0}", startTime));
    Response response = new Response();
    Update update = QueryBuilder.update(keyspaceName, tableName);
    try {
      Session session = connectionManager.getSession(keyspaceName);
      Assignments assignments = update.with();
      Update.Where where = update.where();
      request
          .entrySet()
          .stream()
          .forEach(
              x -> {
                assignments.and(QueryBuilder.set(x.getKey(), x.getValue()));
              });
      compositeKey
          .entrySet()
          .stream()
          .forEach(
              x -> {
                where.and(eq(x.getKey(), x.getValue()));
              });
      Statement updateQuery = where;
      session.execute(updateQuery);
    } catch (Exception e) {
      logger.error(reqContext, Constants.EXCEPTION_MSG_UPDATE + tableName + " : " + e.getMessage());
      if (e.getMessage().contains(Constants.UNKNOWN_IDENTIFIER)) {
        throw new BaseException(
            localizer.getMessage(CassandraUtil.processExceptionForUnknownIdentifier(e), null),
           IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
      }
      throw new BaseException(
              localizer.getMessage(IResponseMessage.SERVER_ERROR, null),
              IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
    }finally {
      logQueryElapseTime("updateRecord", startTime,update.getQueryString(),reqContext);
    }
    return response;
  }

  private Response getRecordByIdentifier(
      String keyspaceName, String tableName, Object key, List<String> fields, Map<String, Object> reqContext) throws BaseException {
    long startTime = System.currentTimeMillis();
    logger.debug(reqContext, MessageFormat.format("Cassandra Service getRecordBy key method started at == {0}", startTime));
    Response response = new Response();
    Builder selectBuilder=null;
    try {
      Session session = connectionManager.getSession(keyspaceName);
      if (CollectionUtils.isNotEmpty(fields)) {
        selectBuilder = QueryBuilder.select(fields.toArray(new String[fields.size()]));
      } else {
        selectBuilder = QueryBuilder.select().all();
      }
      Select selectQuery = selectBuilder.from(keyspaceName, tableName);
      Where selectWhere = selectQuery.where();
      if (key instanceof String) {
        selectWhere.and(eq(Constants.IDENTIFIER, key));
      } else if (key instanceof Map) {
        Map<String, Object> compositeKey = (Map<String, Object>) key;
        compositeKey
            .entrySet()
            .stream()
            .forEach(
                x -> {
                  CassandraUtil.createQuery(x.getKey(), x.getValue(), selectWhere);
                });
      }
      ResultSet results = session.execute(selectWhere);
      response = CassandraUtil.createResponse(results);
    } catch (Exception e) {
      logger.error(reqContext, Constants.EXCEPTION_MSG_FETCH + tableName + " : " + e.getMessage());
      throw new BaseException(
          e.getMessage(),
          IResponseMessage.SERVER_ERROR, ResponseCode.SERVER_ERROR.getCode());
    }finally {
      if(null != selectBuilder){
        logQueryElapseTime("getRecordByIdentifier", startTime,selectBuilder.toString(),reqContext);
      }
    }
    return response;
  }

  @Override
  public Response getRecordById(String keyspaceName, String tableName, String key, Map<String, Object> reqContext)
      throws BaseException {
    return getRecordByIdentifier(keyspaceName, tableName, key, null,reqContext);
  }

  @Override
  public Response getRecordById(String keyspaceName, String tableName, Map<String, Object> key, Map<String, Object> reqContext)
      throws BaseException {
    return getRecordByIdentifier(keyspaceName, tableName, key, null,reqContext);
  }

  @Override
  public Response getRecordById(
      String keyspaceName, String tableName, String key, List<String> fields, Map<String, Object> reqContext) throws BaseException {
    return getRecordByIdentifier(keyspaceName, tableName, key, fields,reqContext);
  }

  @Override
  public Response getRecordById(
      String keyspaceName, String tableName, Map<String, Object> key, List<String> fields, Map<String, Object> reqContext)
      throws BaseException {
    return getRecordByIdentifier(keyspaceName, tableName, key, fields,reqContext);
  }

  @Override
  public Response getRecordWithTTLById(
      String keyspaceName,
      String tableName,
      Map<String, Object> key,
      List<String> ttlFields,
      List<String> fields,
      Map<String, Object> reqContext)
      throws BaseException {
    return getRecordWithTTLByIdentifier(keyspaceName, tableName, key, ttlFields, fields,reqContext);
  }

  public Response getRecordWithTTLByIdentifier(
      String keyspaceName,
      String tableName,
      Map<String, Object> key,
      List<String> ttlFields,
      List<String> fields,
      Map<String, Object> reqContext)
      throws BaseException {
    long startTime = System.currentTimeMillis();
    logger.debug(reqContext, MessageFormat.format("Cassandra Service getRecordBy key method started at == {0}", startTime));
    Response response = new Response();
    Selection select = QueryBuilder.select();
    try {
      Session session = connectionManager.getSession(keyspaceName);
      for (String field : fields) {
        select.column(field);
      }
      for (String field : ttlFields) {
        select.ttl(field).as(field + "_ttl");
      }
      Select.Where selectWhere = select.from(keyspaceName, tableName).where();
      key.entrySet()
          .stream()
          .forEach(
              x -> {
                selectWhere.and(QueryBuilder.eq(x.getKey(), x.getValue()));
              });

      ResultSet results = session.execute(selectWhere);
      response = CassandraUtil.createResponse(results);
      return response;
    } catch (Exception e) {
      logger.error(reqContext, Constants.EXCEPTION_MSG_FETCH + tableName + " : " + e.getMessage());
      throw new BaseException(
          e.getMessage(),
          IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
    }finally {
      logQueryElapseTime("getRecordByIdentifier", startTime,select.toString(),reqContext);
    }
  }

  @Override
  public Response batchInsert(
      String keyspaceName, String tableName, List<Map<String, Object>> records, Map<String, Object> reqContext)
      throws BaseException {

    long startTime = System.currentTimeMillis();
    logger.debug(reqContext, MessageFormat.format("Cassandra Service batchInsert method started at == {0}", startTime));
    Response response = new Response();
    BatchStatement batchStatement = new BatchStatement();
    ResultSet resultSet = null;

    try {
      Session session = connectionManager.getSession(keyspaceName);
      for (Map<String, Object> map : records) {
        Insert insert = QueryBuilder.insertInto(keyspaceName, tableName);
        map.entrySet()
            .stream()
            .forEach(
                x -> {
                  insert.value(x.getKey(), x.getValue());
                });
        batchStatement.add(insert);
      }
      resultSet = session.execute(batchStatement);
      response.put(Constants.RESPONSE, Constants.SUCCESS);
      return response;
    } catch (QueryExecutionException
        | QueryValidationException
        | NoHostAvailableException
        | IllegalStateException e) {
      logger.error(reqContext, "Cassandra Batch Insert Failed." + e.getMessage());
      throw new BaseException(
           e.getMessage(),
          IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
    }finally {
      logQueryElapseTime("batchInsert", startTime,batchStatement.toString(),reqContext);
    }
  }

  /**
   * This method updates all the records in a batch
   *
   * @param keyspaceName
   * @param tableName
   * @param records
   * @return
   */
  // @Override
  public Response batchUpdateById(
      String keyspaceName, String tableName, List<Map<String, Object>> records, Map<String, Object> reqContext)
      throws BaseException {

    long startTime = System.currentTimeMillis();
    logger.debug(reqContext, MessageFormat.format("Cassandra Service batchUpdateById method started at == {0}", startTime));
    Response response = new Response();
    BatchStatement batchStatement = new BatchStatement();
    ResultSet resultSet = null;
    try {
      Session session = connectionManager.getSession(keyspaceName);
      for (Map<String, Object> map : records) {
        Update update = createUpdateStatement(keyspaceName, tableName, map);
        batchStatement.add(update);
      }
      resultSet = session.execute(batchStatement);
      response.put(Constants.RESPONSE, Constants.SUCCESS);
    } catch (QueryExecutionException
        | QueryValidationException
        | NoHostAvailableException
        | IllegalStateException e) {
      logger.error(reqContext, "Cassandra Batch Update Failed." + e.getMessage());
      throw new BaseException(
          e.getMessage(),
          IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
    }finally {
      logQueryElapseTime("batchUpdateById", startTime,batchStatement.getStatements().toString(),reqContext);
    }
    return response;
  }

  /**
   * This method performs batch operations of insert and update on a same table, further other
   * operations can be added to if it is necessary.
   *
   * @param keySpaceName
   * @param tableName
   * @param inputData
   * @return
   */
  @Override
  public Response performBatchAction(
      String keySpaceName, String tableName, Map<String, Object> inputData, Map<String, Object> reqContext) throws BaseException {

    long startTime = System.currentTimeMillis();
    logger.debug(reqContext, MessageFormat.format("Cassandra Service performBatchAction method started at == {0}", startTime));

    Session session = connectionManager.getSession(keySpaceName);
    Response response = new Response();
    BatchStatement batchStatement = new BatchStatement();
    ResultSet resultSet = null;
    try {
      inputData.forEach(
          (key, inputMap) -> {
            Map<String, Object> record = (Map<String, Object>) inputMap;
            if (key.equals(Constants.INSERT)) {
              Insert insert = createInsertStatement(keySpaceName, tableName, record);
              batchStatement.add(insert);
            } else if (key.equals(Constants.UPDATE)) {
              Update update = createUpdateStatement(keySpaceName, tableName, record);
              batchStatement.add(update);
            }
          });
      resultSet = session.execute(batchStatement);
      response.put(Constants.RESPONSE, Constants.SUCCESS);
    } catch (QueryExecutionException
        | QueryValidationException
        | NoHostAvailableException
        | IllegalStateException e) {
      logger.error(reqContext, "Cassandra performBatchAction Failed." + e.getMessage());
      throw new BaseException(
          e.getMessage(),
          IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
    }finally {
      logQueryElapseTime("performBatchAction", startTime,batchStatement.toString(),reqContext);

    }
    return response;
  }

  private Insert createInsertStatement(
      String keySpaceName, String tableName, Map<String, Object> record) {
    Insert insert = QueryBuilder.insertInto(keySpaceName, tableName);
    record
        .entrySet()
        .stream()
        .forEach(
            x -> {
              insert.value(x.getKey(), x.getValue());
            });
    return insert;
  }

  private Update createUpdateStatement(
      String keySpaceName, String tableName, Map<String, Object> record) {
    Update update = QueryBuilder.update(keySpaceName, tableName);
    Assignments assignments = update.with();
    Update.Where where = update.where();
    record
        .entrySet()
        .stream()
        .forEach(
            x -> {
              if (Constants.ID.equals(x.getKey())) {
                where.and(eq(x.getKey(), x.getValue()));
              } else {
                assignments.and(QueryBuilder.set(x.getKey(), x.getValue()));
              }
            });
    return update;
  }

  @Override
  public Response batchUpdate(
      String keyspaceName, String tableName, List<Map<String, Map<String, Object>>> list, Map<String, Object> reqContext)
      throws BaseException {

    BatchStatement batchStatement = new BatchStatement();
    long startTime = System.currentTimeMillis();
    logger.debug(reqContext, MessageFormat.format("Cassandra Service batchUpdate method started at == {0}", startTime));
    Response response = new Response();
    ResultSet resultSet = null;
    try {
      Session session = connectionManager.getSession(keyspaceName);
      for (Map<String, Map<String, Object>> record : list) {
        Map<String, Object> primaryKey = record.get(Constants.PRIMARY_KEY);
        Map<String, Object> nonPKRecord = record.get(Constants.NON_PRIMARY_KEY);
        batchStatement.add(
            CassandraUtil.createUpdateQuery(primaryKey, nonPKRecord, keyspaceName, tableName));
      }
      resultSet = session.execute(batchStatement);
      response.put(Constants.RESPONSE, Constants.SUCCESS);
    } catch (Exception ex) {
      logger.error(reqContext, "Cassandra Batch Update failed " + ex.getMessage());
      throw new BaseException(
              ex.getMessage(),
          IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
    }finally {
      logQueryElapseTime("batchUpdate", startTime,batchStatement.toString(),reqContext);
    }
    return response;
  }

  protected void logQueryElapseTime(String operation, long startTime,String query, Map<String, Object> context) {

    logger.debug(context,  "Cassandra query : " + query);
    long stopTime = System.currentTimeMillis();
    long elapsedTime = stopTime - startTime;
    String message =
            "Cassandra operation {0} started at {1} and completed at {2}. Total time elapsed is {3}.";
    MessageFormat mf = new MessageFormat(message);
    logger.debug( mf.format(new Object[] {operation, startTime, stopTime, elapsedTime}));
  }

  @Override
  public Response getRecordsByIndexedProperty(
      String keyspaceName, String tableName, String propertyName, Object propertyValue, Map<String, Object> reqContext)
      throws BaseException {
    long startTime = System.currentTimeMillis();
    logger.debug(reqContext, MessageFormat.format("CassandraOperationImpl:getRecordsByIndexedProperty called at {0}", startTime));
    Response response = new Response();
    Select selectQuery = QueryBuilder.select().all().from(keyspaceName, tableName);
    try {
      selectQuery.where().and(eq(propertyName, propertyValue));
      ResultSet results =
          connectionManager.getSession(keyspaceName).execute(selectQuery.allowFiltering());
      response = CassandraUtil.createResponse(results);
      return response;
    } catch (Exception e) {
      logger.error(reqContext,
          "CassandraOperationImpl:getRecordsByIndexedProperty: "
              + Constants.EXCEPTION_MSG_FETCH
              + tableName
              + " : "
              + e.getMessage());
      throw new BaseException(
          e.getMessage(),
          IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
    }finally {
      logQueryElapseTime("getRecordsByIndexedProperty", startTime,selectQuery.getQueryString(),reqContext);
    }
  }

  @Override
  public void deleteRecord(
      String keyspaceName, String tableName, Map<String, String> compositeKeyMap, Map<String, Object> reqContext)
      throws BaseException {
    long startTime = System.currentTimeMillis();
    logger.debug(reqContext, MessageFormat.format("CassandraOperationImpl: deleteRecord by composite key called at {0} ", startTime));
    Delete delete = QueryBuilder.delete().from(keyspaceName, tableName);
    try {
      Delete.Where deleteWhere = delete.where();
      compositeKeyMap
          .entrySet()
          .stream()
          .forEach(
              x -> {
                Clause clause = eq(x.getKey(), x.getValue());
                deleteWhere.and(clause);
              });
      connectionManager.getSession(keyspaceName).execute(delete);
    } catch (Exception e) {
      logger.error(reqContext,
          "CassandraOperationImpl: deleteRecord by composite key. "
              + Constants.EXCEPTION_MSG_DELETE
              + tableName
              + " : "
              + e.getMessage());
      throw new BaseException(
          e.getMessage(),
          IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
    }finally {
      logQueryElapseTime("deleteRecordByCompositeKey", startTime,delete.getQueryString(),reqContext);
    }
  }

  @Override
  public boolean deleteRecords(String keyspaceName, String tableName, List<String> identifierList, Map<String, Object> reqContext)
      throws BaseException {
    long startTime = System.currentTimeMillis();
    ResultSet resultSet;
    logger.debug(reqContext,MessageFormat.format("CassandraOperationImpl: deleteRecords called at {0} ", startTime));
    Delete delete = QueryBuilder.delete().from(keyspaceName, tableName);
    try {
      Delete.Where deleteWhere = delete.where();
      Clause clause = QueryBuilder.in(Constants.ID, identifierList);
      deleteWhere.and(clause);
      resultSet = connectionManager.getSession(keyspaceName).execute(delete);
    } catch (Exception e) {
      logger.error(reqContext,
          "CassandraOperationImpl: deleteRecords by list of primary key. "
              + Constants.EXCEPTION_MSG_DELETE
              + tableName
              + " : "
              + e.getMessage());
      throw new BaseException(
          e.getMessage(),
          IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
    }finally {
      logQueryElapseTime("deleteRecords", startTime,delete.getQueryString(),reqContext);

    }
    return resultSet.wasApplied();
  }

  @Override
  public Response getRecordsByCompositeKey(
      String keyspaceName, String tableName, Map<String, Object> compositeKeyMap, Map<String, Object> reqContext)
      throws BaseException {
    long startTime = System.currentTimeMillis();
    logger.debug(reqContext, MessageFormat.format("CassandraOperationImpl: getRecordsByCompositeKey called at {0}", startTime));
    Response response = new Response();
    Builder selectBuilder = QueryBuilder.select().all();
    Select selectQuery = selectBuilder.from(keyspaceName, tableName);
    try {

      Where selectWhere = selectQuery.where();
      for (Entry<String, Object> entry : compositeKeyMap.entrySet()) {
        Clause clause = eq(entry.getKey(), entry.getValue());
        selectWhere.and(clause);
      }
      ResultSet results = connectionManager.getSession(keyspaceName).execute(selectQuery);
      response = CassandraUtil.createResponse(results);
      return response;
    } catch (Exception e) {
      logger.error(reqContext,
          "CassandraOperationImpl:getRecordsByCompositeKey: "
              + Constants.EXCEPTION_MSG_FETCH
              + tableName
              + " : "
              + e.getMessage());
      throw new BaseException(
          e.getMessage(),
          IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
    }finally {
      logQueryElapseTime("getRecordsByCompositeKey", startTime,selectQuery.getQueryString(),reqContext);

    }
  }

  @Override
  public Response getRecordsByIdsWithSpecifiedColumns(
      String keyspaceName, String tableName, List<String> properties, List<String> ids, Map<String, Object> reqContext)
      throws BaseException {
    long startTime = System.currentTimeMillis();
    logger.debug(reqContext, MessageFormat.format(
        "CassandraOperationImpl: getRecordsByIdsWithSpecifiedColumns call started at  {0}",
        startTime));
    Response response = new Response();
    Builder selectBuilder=null;
    try {
      if (CollectionUtils.isNotEmpty(properties)) {
        selectBuilder = QueryBuilder.select(properties.toArray(new String[properties.size()]));
      } else {
        selectBuilder = QueryBuilder.select().all();
      }
      response = executeSelectQuery(keyspaceName, tableName, ids, selectBuilder, "");
      return response;
    } catch (Exception e) {
      logger.error(reqContext,Constants.EXCEPTION_MSG_FETCH + tableName + " : " + e.getMessage(), e);
      throw new BaseException(
          e.getMessage(),
          IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
    }finally {
      if(null != selectBuilder){
        logQueryElapseTime("getRecordsByIdsWithSpecifiedColumns", startTime,selectBuilder.toString(),reqContext);
      }
    }
  }

  private Response executeSelectQuery(
      String keyspaceName,
      String tableName,
      List<String> ids,
      Builder selectBuilder,
      String primaryKeyColumnName) {
    Response response;
    Select selectQuery = selectBuilder.from(keyspaceName, tableName);
    Where selectWhere = selectQuery.where();
    Clause clause = null;
    if (StringUtils.isBlank(primaryKeyColumnName)) {
      clause = QueryBuilder.in(Constants.ID, ids.toArray(new Object[ids.size()]));
    } else {
      clause = QueryBuilder.in(primaryKeyColumnName, ids.toArray(new Object[ids.size()]));
    }

    selectWhere.and(clause);
    ResultSet results = connectionManager.getSession(keyspaceName).execute(selectQuery);
    response = CassandraUtil.createResponse(results);
    return response;
  }

  @Override
  public Response getRecordsByPrimaryKeys(
      String keyspaceName, String tableName, List<String> primaryKeys, String primaryKeyColumnName, Map<String, Object> reqContext)
      throws BaseException {
    long startTime = System.currentTimeMillis();
    logger.debug(reqContext, MessageFormat.format("CassandraOperationImpl: getRecordsByPrimaryKeys call started at {0}", startTime));
    Response response = new Response();
    Builder selectBuilder = QueryBuilder.select().all();
    try {
        response =
                executeSelectQuery(
                        keyspaceName, tableName, primaryKeys, selectBuilder, primaryKeyColumnName);
    } catch (Exception e) {
       logger.error( reqContext,Constants.EXCEPTION_MSG_FETCH + tableName + " : " + e.getMessage());
       throw new BaseException(
                e.getMessage(),
                IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
    }finally {
      logQueryElapseTime("getRecordsByPrimaryKeys", startTime,selectBuilder.toString(),reqContext);
    }
    return response;

  }

  @Override
  public Response insertRecordWithTTL(
      String keyspaceName, String tableName, Map<String, Object> request, int ttl, Map<String, Object> reqContext) throws BaseException {
    long startTime = System.currentTimeMillis();
    Insert insert = QueryBuilder.insertInto(keyspaceName, tableName);
    try {
      request
              .entrySet()
              .stream()
              .forEach(
                      x -> {
                        insert.value(x.getKey(), x.getValue());
                      });
      insert.using(QueryBuilder.ttl(ttl));
      logger.debug(reqContext,"CassandraOperationImpl:insertRecordWithTTL: query = " + insert.getQueryString());
      ResultSet results = connectionManager.getSession(keyspaceName).execute(insert);
      Response response = CassandraUtil.createResponse(results);
      return response;
    }catch (Exception e) {
      logger.error( reqContext, tableName + " : " + e.getMessage());
      throw new BaseException(
              e.getMessage(),
              IResponseMessage.SERVER_ERROR, ResponseCode.SERVER_ERROR.getCode());
    }finally {
      if(null != insert){
        logQueryElapseTime("insertRecordWithTTL", startTime,insert.getQueryString(),reqContext);
      }

    }
  }

  @Override
  public Response updateRecordWithTTL(
      String keyspaceName,
      String tableName,
      Map<String, Object> request,
      Map<String, Object> compositeKey,
      int ttl,
      Map<String, Object> reqContext)
      throws BaseException {
    long startTime = System.currentTimeMillis();
    Update update = QueryBuilder.update(keyspaceName, tableName);
    try {
      Session session = connectionManager.getSession(keyspaceName);
      Assignments assignments = update.with();
      Update.Where where = update.where();
      request
              .entrySet()
              .stream()
              .forEach(
                      x -> {
                        assignments.and(QueryBuilder.set(x.getKey(), x.getValue()));
                      });
      compositeKey
              .entrySet()
              .stream()
              .forEach(
                      x -> {
                        where.and(eq(x.getKey(), x.getValue()));
                      });
      update.using(QueryBuilder.ttl(ttl));
      logger.debug(reqContext,"CassandraOperationImpl:updateRecordWithTTL: query = " + update.getQueryString());
      ResultSet results = session.execute(update);
      Response response = CassandraUtil.createResponse(results);
      return response;
    }catch (Exception e) {
      logger.error( reqContext, tableName + " : " + e.getMessage());
      throw new BaseException(
              e.getMessage(),
              IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
    }finally {
      if(null != update){
        logQueryElapseTime("updateRecordWithTTL", startTime,update.getQueryString(),reqContext);
      }

    }
  }

  @Override
  public Response getRecordsByIdsWithSpecifiedColumnsAndTTL(
      String keyspaceName,
      String tableName,
      Map<String, Object> primaryKeys,
      List<String> properties,
      Map<String, String> ttlPropertiesWithAlias,
      Map<String, Object> reqContext)
      throws BaseException {
    long startTime = System.currentTimeMillis();
    logger.debug(reqContext,
        "CassandraOperationImpl:getRecordsByIdsWithSpecifiedColumnsAndTTL: call started at "
            + startTime);
    Response response = new Response();
    String query = null;
    try {
      Selection selection = QueryBuilder.select();
      if (CollectionUtils.isNotEmpty(properties)) {
        properties
            .stream()
            .forEach(
                property -> {
                  selection.column(property);
                });
      }

      if (MapUtils.isNotEmpty(ttlPropertiesWithAlias)) {
        for (Map.Entry<String, String> entry : ttlPropertiesWithAlias.entrySet()) {
          if (StringUtils.isBlank(entry.getValue())) {
            String errorMsg="CassandraOperationImpl:getRecordsByIdsWithSpecifiedColumnsAndTTL: Alias not provided for ttl key = "
                    + entry.getKey();
            logger.error(reqContext,errorMsg);
            throw new BaseException(
                    errorMsg,
                IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
          }
          selection.ttl(entry.getKey()).as(entry.getValue());
        }
      }
      Select select = selection.from(keyspaceName, tableName);
      primaryKeys
          .entrySet()
          .stream()
          .forEach(
              primaryKey -> {
                select.where().and(eq(primaryKey.getKey(), primaryKey.getValue()));
              });
      query = select.getQueryString();
      logger.debug(reqContext,"Query" + query);
      ResultSet results = connectionManager.getSession(keyspaceName).execute(select);
      response = CassandraUtil.createResponse(results);
    } catch (Exception e) {
      logger.error(Constants.EXCEPTION_MSG_FETCH + tableName + " : " + e.getMessage(), e);
      throw new BaseException(
          e.getMessage(),
              IResponseMessage.SERVER_ERROR, ResponseCode.SERVER_ERROR.getCode());
    }finally {
      if(null != query){
        logQueryElapseTime("getRecordsByIdsWithSpecifiedColumnsAndTTL", startTime,query,reqContext);
      }

    }
    return response;
  }

  @Override
  public Response batchInsertWithTTL(
      String keyspaceName, String tableName, List<Map<String, Object>> records, List<Integer> ttls, Map<String, Object> reqContext)
      throws BaseException {
    long startTime = System.currentTimeMillis();
    logger.debug("CassandraOperationImpl:batchInsertWithTTL: call started at " + startTime);
    if (CollectionUtils.isEmpty(records) || CollectionUtils.isEmpty(ttls)) {
      String errorMsg="CassandraOperationImpl:batchInsertWithTTL: records or ttls is empty";
      logger.error(reqContext,errorMsg);
      throw new BaseException(
          errorMsg,
          IResponseMessage.SERVER_ERROR, ResponseCode.SERVER_ERROR.getCode());
    }
    if (ttls.size() != records.size()) {
      String errorMsg="CassandraOperationImpl:batchInsertWithTTL: Mismatch of records and ttls list size";
      logger.error(reqContext,errorMsg);
      throw new BaseException(
          errorMsg,
          IResponseMessage.SERVER_ERROR, ResponseCode.SERVER_ERROR.getCode());
    }

    Response response = new Response();
    BatchStatement batchStatement = new BatchStatement();
    ResultSet resultSet = null;
    Iterator<Integer> ttlIterator = ttls.iterator();
    try {
      Session session = connectionManager.getSession(keyspaceName);
      for (Map<String, Object> map : records) {
        Insert insert = QueryBuilder.insertInto(keyspaceName, tableName);
        map.entrySet()
            .stream()
            .forEach(
                x -> {
                  insert.value(x.getKey(), x.getValue());
                });
        if (ttlIterator.hasNext()) {
          Integer ttlVal = ttlIterator.next();
          if (ttlVal != null && ttlVal > 0) {
            insert.using(QueryBuilder.ttl(ttlVal));
          }
        }
        batchStatement.add(insert);
      }
      resultSet = session.execute(batchStatement);
      response.put(Constants.RESPONSE, Constants.SUCCESS);
      return response;
    } catch (QueryExecutionException
        | QueryValidationException
        | NoHostAvailableException
        | IllegalStateException e) {
      logger.error(reqContext,
          "CassandraOperationImpl:batchInsertWithTTL: Exception occurred with error message = "
              + e.getMessage(),
          e);
      throw new BaseException(
          e.getMessage(),
          IResponseMessage.SERVER_ERROR, ResponseCode.SERVER_ERROR.getCode());
    }finally {
      logQueryElapseTime("batchInsertWithTTL", startTime,batchStatement.toString(),reqContext);
    }
  }

  @Override
  public Response getRecordByObjectType(
      String keyspace,
      String tableName,
      String columnName,
      String key,
      int value,
      String objectType,
      Map<String, Object> reqContext)
      throws BaseException {
    long startTime = System.currentTimeMillis();
    Select selectQuery = QueryBuilder.select().column(columnName).from(keyspace, tableName);
    try {
      Clause clause = QueryBuilder.lt(key, value);
      selectQuery.where(eq(Constants.OBJECT_TYPE, objectType)).and(clause);
      selectQuery.allowFiltering();
      ResultSet resultSet = connectionManager.getSession(keyspace).execute(selectQuery);
      Response response = CassandraUtil.createResponse(resultSet);
      return response;
     }catch (Exception e){
      logger.error(reqContext,
              "CassandraOperationImpl:getRecordByObjectType: Exception occurred with error message = "
                      + e.getMessage(),
              e);
      throw new BaseException(
              e.getMessage(),
              IResponseMessage.SERVER_ERROR, ResponseCode.SERVER_ERROR.getCode());
    }finally {
      logQueryElapseTime("getRecordByObjectType",startTime,selectQuery.getQueryString(),reqContext);
    }
  }

  @Override
  public Response getRecords(
      String keyspace, String table, Map<String, Object> filters, List<String> fields,Map<String, Object> reqContext)
      throws BaseException, BaseException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void applyOperationOnRecordsAsync(
      String keySpace,
      String table,
      Map<String, Object> filters,
      List<String> fields,
      FutureCallback<ResultSet> callback,
      Map<String, Object> reqContext)
      throws BaseException {
    // TODO Auto-generated method stub

  }

  @Override
  public Response searchValueInList(String keyspace, String tableName, String key, String value,
                                    Map<String, Object> reqContext)
      throws BaseException {
    return searchValueInList(keyspace, tableName, key, value, null,reqContext);
  }

  @Override
  public Response searchValueInList(
      String keyspace, String tableName, String key, String value, Map<String, Object> propertyMap,Map<String, Object> reqContext) throws BaseException {
    long startTime = System.currentTimeMillis();
    Select selectQuery = QueryBuilder.select().all().from(keyspace, tableName);
    try {
      Clause clause = QueryBuilder.contains(key, value);

      selectQuery.where(clause);
      if (MapUtils.isNotEmpty(propertyMap)) {
        for (Entry<String, Object> entry : propertyMap.entrySet()) {
          if (entry.getValue() instanceof List) {
            List<Object> list = (List) entry.getValue();
            if (null != list) {
              Object[] propertyValues = list.toArray(new Object[list.size()]);
              Clause clauseList = QueryBuilder.in(entry.getKey(), propertyValues);
              selectQuery.where(clauseList);
            }
          } else {
            Clause clauseMap = eq(entry.getKey(), entry.getValue());
            selectQuery.where(clauseMap);
          }
        }
      }
      ResultSet resultSet = connectionManager.getSession(keyspace).execute(selectQuery);
      Response response = CassandraUtil.createResponse(resultSet);
      return response;
    }catch (Exception e){
      logger.error(reqContext,
              "CassandraOperationImpl:searchValueInList: Exception occurred with error message = "
                      + e.getMessage(),
              e);
      throw new BaseException(
              e.getMessage(),
              IResponseMessage.SERVER_ERROR, ResponseCode.SERVER_ERROR.getCode());
    }finally {
        logQueryElapseTime("searchValueInList",startTime,selectQuery.getQueryString(),reqContext);
    }
  }

  @Override
  public Response executeSelectQuery(
      String keyspaceName,
      String tableName,
      Map<String, Object> propertyMap,
      Builder selectBuilder, Map<String, Object> reqContext) throws BaseException {
    long startTime = System.currentTimeMillis();
    Response response;
    Select selectQuery = selectBuilder.from(keyspaceName, tableName);
    try {
      if (MapUtils.isNotEmpty(propertyMap)) {
        Where selectWhere = selectQuery.where();
        for (Entry<String, Object> entry : propertyMap.entrySet()) {
          if (entry.getValue() instanceof List) {
            List<Object> list = (List) entry.getValue();
            if (null != list) {
              Object[] propertyValues = list.toArray(new Object[list.size()]);
              Clause clause = QueryBuilder.in(entry.getKey(), propertyValues);
              selectWhere.and(clause);
            }
          } else {
            Clause clause = eq(entry.getKey(), entry.getValue());
            selectWhere.and(clause);
          }
        }
      }
      ResultSet results = connectionManager.getSession(keyspaceName).execute(selectQuery);
      response = CassandraUtil.createResponse(results);
      return response;
    }catch (Exception e){
      logger.error(reqContext,
              "CassandraOperationImpl:executeSelectQuery: Exception occurred with error message = "
                      + e.getMessage(),
              e);
      throw new BaseException(
              e.getMessage(),
              IResponseMessage.SERVER_ERROR, ResponseCode.SERVER_ERROR.getCode());
    }finally {
      logQueryElapseTime("executeSelectQuery",startTime,selectQuery.getQueryString(),reqContext);
    }
  }

  @Override
  public Response batchDelete(String keyspaceName, String tableName, List<Map<String, Object>> list, Map<String, Object> reqContext)
      throws BaseException {
    long startTime = System.currentTimeMillis();
    logger.debug(reqContext,MessageFormat.format("Cassandra Service batchDelete method started at == {0}", startTime));
    Response response = new Response();
    ResultSet resultSet = null;
    BatchStatement batchStatement = new BatchStatement();
    try {
      Session session = connectionManager.getSession(keyspaceName);
      for (Map<String, Object> primaryKey : list) {
        batchStatement.add(CassandraUtil.createDeleteQuery(primaryKey, keyspaceName, tableName));
      }
      resultSet = session.execute(batchStatement);
      response.put(Constants.RESPONSE, Constants.SUCCESS);
      return response;
    } catch (Exception ex) {
      logger.error(reqContext,"Cassandra Batch Delete failed " + ex.getMessage());
      throw new BaseException(
              ex.getMessage(),
          IResponseMessage.SERVER_ERROR, ResponseCode.SERVER_ERROR.getCode());
    }finally {
      logQueryElapseTime("batchDelete", startTime,batchStatement.toString(),reqContext);
    }
  }

  protected String getLocalizedMessage(String key, Locale locale) {
    return localizer.getMessage(key, locale);
  }
}
