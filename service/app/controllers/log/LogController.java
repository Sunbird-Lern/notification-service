package controllers.log;

import controllers.BaseController;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

/** This controller is responsible to manage the dynamic configuration of Logs */
public class LogController extends BaseController {

  /**
   * This action method is responsible to set the Log Level dynamically using Api.
   *
   * @return
   */
  public CompletionStage<Result> setLogLevel() {
    return handleLogRequest();
  }
}
