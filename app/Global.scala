import play.api._
import play.api.mvc._
import play.libs._
import play.api.Play.current
import play.cache.Cache

object Global extends GlobalSettings {

  override def onStart(app: Application) = {
//    play.api.cache.Cache.getAs[String]("mykey")
  }
}