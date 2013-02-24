import play.api._
import play.api.mvc._

object Global extends GlobalSettings {

  override def onStart(app: Application) = {
    Logger.info("Application has started")
  }

  override def onStop(app: Application) = {
    Logger.info("Application stopping")
  }

  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
    Logger("routing").debug("request:" + request.toString)
    super.onRouteRequest(request)
  }

}
