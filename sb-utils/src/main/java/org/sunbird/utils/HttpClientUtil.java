package org.sunbird.utils;


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.MapUtils;
import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.sunbird.common.util.LoggerEnum;
import org.sunbird.request.LoggerUtil;

public class HttpClientUtil {
    private static CloseableHttpClient httpclient = null;
    private static HttpClientUtil httpClientUtil;

    private static LoggerUtil logger =new LoggerUtil(HttpClientUtil.class);

    private HttpClientUtil() {
        ConnectionKeepAliveStrategy keepAliveStrategy =
                (response, context) -> {
                    HeaderElementIterator it =
                            new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                    while (it.hasNext()) {
                        HeaderElement he = it.nextElement();
                        String param = he.getName();
                        String value = he.getValue();
                        if (value != null && param.equalsIgnoreCase("timeout")) {
                            return Long.parseLong(value) * 1000;
                        }
                    }
                    return 180 * 1000;
                };

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(150);
        connectionManager.closeIdleConnections(180, TimeUnit.SECONDS);
        httpclient =
                HttpClients.custom()
                        .setConnectionManager(connectionManager)
                        .useSystemProperties()
                        .setKeepAliveStrategy(keepAliveStrategy)
                        .build();
    }

    public static HttpClientUtil getInstance() {
        if (httpClientUtil == null) {
            synchronized (HttpClientUtil.class) {
                if (httpClientUtil == null) {
                    httpClientUtil = new HttpClientUtil();
                }
            }
        }
        return httpClientUtil;
    }

    public static String get(String requestURL, Map<String, String> headers, Map<String,Object> reqContext) {
        CloseableHttpResponse response = null;
        try {
            HttpGet httpGet = new HttpGet(requestURL);
            if (MapUtils.isNotEmpty(headers)) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpGet.addHeader(entry.getKey(), entry.getValue());
                }
            }
            response = httpclient.execute(httpGet);
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity httpEntity = response.getEntity();
                byte[] bytes = EntityUtils.toByteArray(httpEntity);
                StatusLine sl = response.getStatusLine();
                logger.info(
                        reqContext, "Response from get call : " + sl.getStatusCode() + " - " + sl.getReasonPhrase()+
                                LoggerEnum.INFO.name());
                return new String(bytes);
            } else {
                return "";
            }
        } catch (Exception ex) {
            logger.error(reqContext,"Exception occurred while calling get method: "+ex.getMessage());
            return "";
        } finally {
            if (null != response) {
                try {
                    response.close();
                } catch (Exception ex) {
                    logger.error(reqContext,"Exception occurred while closing get response object: "+ex.getMessage());
                }
            }
        }
    }

    public static String post(String requestURL, String params, Map<String, String> headers, Map<String,Object> reqContext) {
        CloseableHttpResponse response = null;
        try {
            logger.info(reqContext,
                    "Calling Request api: "
                            + requestURL
                            + " with request: "
                            + params
                            + " with headers: "
                            + headers);
            HttpPost httpPost = new HttpPost(requestURL);
            if (MapUtils.isNotEmpty(headers)) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpPost.addHeader(entry.getKey(), entry.getValue());
                }
            }
            StringEntity entity = new StringEntity(params);
            httpPost.setEntity(entity);

            response = httpclient.execute(httpPost);
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity httpEntity = response.getEntity();
                byte[] bytes = EntityUtils.toByteArray(httpEntity);
                StatusLine sl = response.getStatusLine();
                logger.info(reqContext,
                        "Response from post call : " + sl.getStatusCode() + " - " + sl.getReasonPhrase()+LoggerEnum.INFO.name());
                return new String(bytes);
            } else {
                return "";
            }
        } catch (Exception ex) {
            logger.error(reqContext,"Exception occurred while calling Post method: "+ ex.getMessage());
            return "";
        } finally {
            if (null != response) {
                try {
                    response.close();
                } catch (Exception ex) {
                    logger.error(reqContext,"Exception occurred while closing Post response object "+ ex.getMessage());
                }
            }
        }
    }

    public static String postFormData(
            String requestURL, Map<String, String> params, Map<String, String> headers, Map<String,Object> reqContext) {
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(requestURL);
            if (MapUtils.isNotEmpty(headers)) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpPost.addHeader(entry.getKey(), entry.getValue());
                }
            }

            List<NameValuePair> form = new ArrayList<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                form.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);

            httpPost.setEntity(entity);

            response = httpclient.execute(httpPost);
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity httpEntity = response.getEntity();
                byte[] bytes = EntityUtils.toByteArray(httpEntity);
                StatusLine sl = response.getStatusLine();
                logger.info(reqContext,MessageFormat.format("Response from post call : {0} - {1}", sl.getStatusCode(), sl.getReasonPhrase()));
                return new String(bytes);
            } else {
                return "";
            }
        } catch (Exception ex) {
            logger.error(reqContext,MessageFormat.format("Exception occurred while calling Post method {0}", ex.getMessage()));
            return "";
        } finally {
            if (null != response) {
                try {
                    response.close();
                } catch (Exception ex) {
                    logger.error(reqContext,MessageFormat.format("Exception occurred while closing Post response object: {0}", ex.getMessage()));
                }
            }
        }
    }

    public static String patch(String requestURL, String params, Map<String, String> headers, Map<String,Object> reqContext) {
        CloseableHttpResponse response = null;
        try {
            HttpPatch httpPatch = new HttpPatch(requestURL);
            if (MapUtils.isNotEmpty(headers)) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpPatch.addHeader(entry.getKey(), entry.getValue());
                }
            }
            StringEntity entity = new StringEntity(params);
            httpPatch.setEntity(entity);

            response = httpclient.execute(httpPatch);
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity httpEntity = response.getEntity();
                byte[] bytes = EntityUtils.toByteArray(httpEntity);
                StatusLine sl = response.getStatusLine();
                logger.error(reqContext,
                        MessageFormat.format("Response from patch call : {0} - {1} ", sl.getStatusCode(), sl.getReasonPhrase()));
                return new String(bytes);
            } else {
                return "";
            }
        } catch (Exception ex) {
            logger.error(reqContext,MessageFormat.format("Exception occurred while calling patch method {0}", ex.getMessage()));
            return "";
        } finally {
            if (null != response) {
                try {
                    response.close();
                } catch (Exception ex) {
                    logger.error(reqContext,MessageFormat.format("Exception occurred while closing patch response object {0}", ex.getMessage()));
                }
            }
        }
    }
}
