package utils;

import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.sunbird.Application;

import org.sunbird.auth.verifier.KeyManager;
import org.sunbird.common.exception.BaseException;
import org.sunbird.request.LoggerUtil;
import org.sunbird.util.DBUtil;
import play.api.Environment;
import play.api.inject.ApplicationLifecycle;

/**
 * This class will be called after on application startup. only one instance of this class will be
 * created. StartModule class has responsibility to eager load this class.
 *
 * @author manzarul
 */
@Singleton
public class ApplicationStart {
  private static LoggerUtil logger = new LoggerUtil(ApplicationStart.class);
	/**
	   *
	   * All one time initialization which required during server startup will fall here.
	   * @param lifecycle ApplicationLifecycle
	   * @param environment Environment
	   */
	  @Inject
	  public ApplicationStart(ApplicationLifecycle lifecycle, Environment environment) throws BaseException {
		  Application.getInstance().init();
		  checkCassandraConnections();
	    // Shut-down hook
	    lifecycle.addStopHook(
	        () -> {
	          return CompletableFuture.completedFuture(null);
	        });
		  KeyManager.init();
	  }


	private static void checkCassandraConnections() throws BaseException {
		DBUtil.checkCassandraDbConnections();
	}
}
