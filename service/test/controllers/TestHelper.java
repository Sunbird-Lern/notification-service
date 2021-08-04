package controllers;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.route;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

/**
 * This a helper class for All the Controllers Test
 *
 * @author anmolgupta
 */
public class TestHelper extends WithApplication {

  private ObjectMapper mapperObj = new ObjectMapper();


  /**
   * This method will perform a request call.
   *
   * @param url
   * @param method
   * @param requestMap
   * @param headerMap
   * @return Result
   */
  public Result performTest(String url, String method, Map requestMap, Map headerMap) {
    String data = mapToJson(requestMap);
    Http.RequestBuilder req;
    if (StringUtils.isNotBlank(data) && !requestMap.isEmpty()) {
      JsonNode json = Json.parse(data);
      req = new Http.RequestBuilder().bodyJson(json).uri(url).method(method);
    } else {
      req = new Http.RequestBuilder().uri(url).method(method);
    }
    //req.headers(headerMap);
    Result result = route(fakeApplication(), req);
    return result;
  }

  /**
   * This method is responsible for converting map to json
   *
   * @param map
   * @return String
   */
  public String mapToJson(Map map) {
    String jsonResp = "";

    if (map != null) {
      try {
        jsonResp = mapperObj.writeValueAsString(map);
      } catch (IOException e) {
      }
    }
    return jsonResp;
  }

  /**
   * This method is used to return the status Code for the perform request
   *
   * @param result
   * @return
   */
  public int getResponseStatus(Result result) {
    return result.status();
  }

  /**
   * This method will return the headerMap required for Apis.
   *
   * @return
   */
  public Map<String, String[]> getHeaderMap() {
    Map<String, String[]> headerMap = new HashMap<>();
    headerMap.put("x-authenticated-user-token", new String[] {"Some authenticated user ID"});
    headerMap.put("Authorization", new String[] {"Bearer ...."});
    return headerMap;
  }

  public Map<String, String[]> getUserHeaderMap() {
    Map<String, String[]> headerMap = new HashMap<>();
    headerMap.put("x-authenticated-user-token", new String[] {"Some authenticated user ID"});
    headerMap.put("Authorization", new String[] {"Bearer ...."});
    headerMap.put("Accept", new String[] {"application/json"});
    headerMap.put("Content-Type", new String[] {"application/json"});
    return headerMap;
  }
}