package utils;

import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.sunbird.Application;

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
	Logger logger = Logger.getLogger(ApplicationStart.class);
	/**
	   *
	   * All one time initialization which required during server startup will fall here.
	   * @param lifecycle ApplicationLifecycle
	   * @param environment Environment
	   */
	  @Inject
	  public ApplicationStart(ApplicationLifecycle lifecycle, Environment environment) {
		  Application.getInstance().init();
		  BasicConfigurator.configure();
	    // Shut-down hook
	    lifecycle.addStopHook(
	        () -> {
	          return CompletableFuture.completedFuture(null);
	        });
	  }
}
