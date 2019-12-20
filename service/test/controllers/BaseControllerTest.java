package controllers;

import akka.actor.ActorRef;
<<<<<<< HEAD

=======
import akka.dispatch.Futures;
import akka.pattern.Patterns;
import akka.util.Timeout;
>>>>>>> upstream/release-1.1.0
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
<<<<<<< HEAD

=======
>>>>>>> upstream/release-1.1.0
import org.sunbird.message.Localizer;
import org.sunbird.request.Request;
import org.sunbird.response.Response;
<<<<<<< HEAD
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import java.util.Map;

=======
import scala.compat.java8.FutureConverters;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.util.Map;

import static org.mockito.Mockito.when;
>>>>>>> upstream/release-1.1.0


@RunWith(PowerMockRunner.class)
@PrepareForTest({org.sunbird.Application.class, BaseController.class, ActorRef.class,Patterns.class,FutureConverters.class})
@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*", "javax.security.*"})

public class BaseControllerTest {
	Localizer localizer = Localizer.getInstance();
	public TestHelper testHelper;
	public static Map<String, String[]> headerMap;
	private org.sunbird.Application application;
	private static ActorRef actorRef;
	private static BaseController baseController;

	public BaseControllerTest() {
		testHelper = new TestHelper();
		headerMap = testHelper.getHeaderMap();
		baseControllerTestsetUp();
	}

	public void baseControllerTestsetUp() {

		application = PowerMockito.mock(org.sunbird.Application.class);
		PowerMockito.mockStatic(org.sunbird.Application.class);
		PowerMockito.when(org.sunbird.Application.getInstance()).thenReturn(application);
		application.init();
		mockRequestHandler();
	}


	public void mockRequestHandler() {

		try {
			baseController = Mockito.mock(BaseController.class);
			actorRef = Mockito.mock(ActorRef.class);
<<<<<<< HEAD
			Mockito.when(baseController.getActorRef(Mockito.anyString())).thenReturn(actorRef);
			PowerMockito.mockStatic(Await.class);
			PowerMockito.when(Await.result(Mockito.any(Future.class), Mockito.any(FiniteDuration.class)))
							.thenReturn(getResponseObject());
		}catch (Exception ex) {
=======
			when(baseController.getActorRef(Mockito.anyString())).thenReturn(actorRef);
			PowerMockito.mockStatic(Patterns.class);
			Future<Object>f1= Futures.successful(getResponseObject());
			when(Patterns.ask(Mockito.any(ActorRef.class),Mockito.any(Request.class),Mockito.any(Timeout.class))).thenReturn(f1);
		}catch (Exception ex) {
			ex.printStackTrace();
>>>>>>> upstream/release-1.1.0
		}
	}

	private Response getResponseObject() {

		Response response = new Response();
		response.put("ResponseCode", "success");
		return response;
	}

	  @Test
	  public void getTimeStampSuccess() {
		 Long val = new BaseController().getTimeStamp();
		 Assert.assertTrue(val<=System.currentTimeMillis());
	  }
}