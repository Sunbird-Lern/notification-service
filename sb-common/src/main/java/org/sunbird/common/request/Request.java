package org.sunbird.common.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.ActorServiceException;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.Localizer;
import org.sunbird.common.message.ResponseCode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/** @author Manzarul */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Request implements Serializable {

  private static final long serialVersionUID = -2362783406031347676L;
  private static final Integer MIN_TIMEOUT = 0;
  private static final Integer MAX_TIMEOUT = 30;
  private static final int WAIT_TIME_VALUE = 30;

  private String id;
  private String ver;
  private String ts;
  private RequestParams params;

  // Request context
  private RequestContext requestContext;

  private Map<String, Object> context;

  private Map<String, Object> request = new HashMap<>();

  private String managerName;
  private String operation;
  private String requestId;
  private int env;
  protected String path;

  private Integer timeout; // in seconds

  public Request() {
    this.context = new WeakHashMap<>();
    this.params = new RequestParams();
    this.params.setMsgid(requestId);
  }


  public Request(Request request) {
    this.params = request.getParams();
    if (null == this.params) this.params = new RequestParams();
    if (StringUtils.isBlank(this.params.getMsgid()) && !StringUtils.isBlank(requestId))
      this.params.setMsgid(requestId);
  }

  public String getRequestId() {
    if (null != this.params) return this.params.getMsgid();
    return requestId;
  }

  public RequestContext getRequestContext() {
    return requestContext;
  }

  public void setRequestContext(RequestContext requestContext) {
    this.requestContext = requestContext;
  }

  public Map<String, Object> getContext() {
    return context;
  }

  public void setContext(Map<String, Object> context) {
    this.context = context;
  }

  /** @return the requestValueObjects */
  public Map<String, Object> getRequest() {
    return request;
  }

  public void setRequest(Map<String, Object> request) {
    this.request = request;
  }

  public Object get(String key) {
    return request.get(key);
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public void put(String key, Object vo) {
    request.put(key, vo);
  }

  public String getManagerName() {
    return managerName;
  }

  public void setManagerName(String managerName) {
    this.managerName = managerName;
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public void copyRequestValueObjects(Map<String, Object> map) {
    if (null != map && map.size() > 0) {
      this.request.putAll(map);
    }
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getVer() {
    return ver;
  }

  public void setVer(String ver) {
    this.ver = ver;
  }

  public String getTs() {
    return ts;
  }

  public void setTs(String ts) {
    this.ts = ts;
  }

  public RequestParams getParams() {
    return params;
  }

  public void setParams(RequestParams params) {
    this.params = params;
    if (this.params.getMsgid() == null && requestId != null) this.params.setMsgid(requestId);
  }

  /** @return the env */
  public int getEnv() {
    return env;
  }

  /** @param env the env to set */
  public void setEnv(int env) {
    this.env = env;
  }

  public Integer getTimeout() {
    return timeout == null ? WAIT_TIME_VALUE : timeout;
  }

  public void setTimeout(Integer timeout) throws BaseException {
    if (timeout < MIN_TIMEOUT && timeout > MAX_TIMEOUT) {
      throw new ActorServiceException.InvalidRequestTimeout(
              IResponseMessage.INVALID_OPERATION_NAME,
              Localizer.getInstance().getMessage(IResponseMessage.INVALID_OPERATION_NAME,null),
              ResponseCode.CLIENT_ERROR.getCode());
    }
    this.timeout = timeout;
  }
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }
}