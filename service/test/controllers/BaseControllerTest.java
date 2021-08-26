package controllers;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.Futures;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.request.Request;
import org.sunbird.common.response.Response;
import org.sunbird.common.response.ResponseParams;
import org.sunbird.common.util.JsonKey;
import play.Application;
import play.Mode;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import scala.compat.java8.FutureConverters;
import scala.concurrent.Future;
import utils.module.OnRequestHandler;
import utils.module.RequestInterceptor;
import utils.module.StartModule;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;


@RunWith(PowerMockRunner.class)
@PrepareForTest({org.sunbird.Application.class, BaseController.class, ActorRef.class, OnRequestHandler.class, RequestInterceptor.class})
@PowerMockIgnore({
  "javax.management.*",
  "javax.net.ssl.*",
  "javax.security.*",
  "jdk.internal.reflect.*",
  "javax.crypto.*",
  "javax.script.*",
  "javax.xml.*",
  "com.sun.org.apache.xerces.*",
  "org.xml.*"
})

public class BaseControllerTest {
	protected Application application;
	private ActorSystem system;
	private Props props;
	private org.sunbird.Application app;
	private static ActorRef actorRef;

	public <T> void setup(Class<T> actorClass) {
		try {
			application =
					new GuiceApplicationBuilder()
							.in(new File("path/to/app"))
							.in(Mode.TEST)
							.disable(StartModule.class)
							.build();
			Helpers.start(application);
			system = ActorSystem.create("system");
			props = Props.create(actorClass);
			actorRef = system.actorOf(props);
			applicationSetUp();

			Map userAuthentication = new HashMap<String, String>();
			userAuthentication.put(JsonKey.USER_ID, "userId");

			PowerMockito.mockStatic(RequestInterceptor.class);
			PowerMockito.when(RequestInterceptor.verifyRequestData(Mockito.any()))
					.thenReturn(userAuthentication);
			PowerMockito.mockStatic(OnRequestHandler.class);
		} catch (Exception e) {
			System.out.println("exception occurred " + e.getMessage());
		}
	}

	public void applicationSetUp() throws BaseException {
		app = PowerMockito.mock(org.sunbird.Application.class);
		PowerMockito.mockStatic(org.sunbird.Application.class);
		PowerMockito.when(org.sunbird.Application.getInstance()).thenReturn(app);
		PowerMockito.when(app.getActorRef(Mockito.anyString())).thenReturn(actorRef);
		app.init();
	}

	private Response getResponseObject() {

		Response response = new Response();
		response.put("ResponseCode", "success");
		return response;
	}

	public Result performTest(String url, String method) {
		Http.RequestBuilder req = new Http.RequestBuilder().uri(url).method(method);
		Result result = Helpers.route(application, req);
		return result;
	}

	public String getResponseCode(Result result) {
		String responseStr = Helpers.contentAsString(result);
		ObjectMapper mapper = new ObjectMapper();
		try {
			Response response = mapper.readValue(responseStr, Response.class);
			if (response != null) {
				ResponseParams params = response.getParams();
				return params.getStatus();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public int getResponseStatus(Result result) {
		return result.status();
	}

	public Result performTest(String url, String method, Map map) {
		String data = mapToJson(map);
		Http.RequestBuilder req;
		if (StringUtils.isNotBlank(data)) {
			JsonNode json = Json.parse(data);
			req = new Http.RequestBuilder().bodyJson(json).uri(url).method(method);
		} else {
			req = new Http.RequestBuilder().uri(url).method(method);
		}
		Result result = Helpers.route(application, req);
		return result;
	}

	public String mapToJson(Map map) {
		ObjectMapper mapperObj = new ObjectMapper();
		String jsonResp = "";

		if (map != null) {
			try {
				jsonResp = mapperObj.writeValueAsString(map);
			} catch (IOException e) {
			}
		}
		return jsonResp;
	}
	  @Test
	  public void getTimeStampSuccess() {
		 Long val = new BaseController().getTimeStamp();
		 Assert.assertTrue(val<=System.currentTimeMillis());
	  }
}