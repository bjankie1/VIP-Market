package controllers

import play.api.data._
import play.api.data.Forms._
import play.api.templates._
import models._
import views._
import play.api.mvc._
import play.api.mvc.Results._
import jp.t2v.lab.play20.auth._
import play.api.Play._
import play.api.cache.Cache
import reflect.{ClassTag, classTag}
import play.api.Logger
import play.api.libs.Crypto

trait AuthConfigImpl extends AuthConfig {

  /**
   * A type that is used to identify a user.
   * `String`, `Int`, `Long` and so on.
   */
  type Id = String

  /**
   * A type that represents a user in your application.
   * `User`, `Account` and so on.
   */
  type User = Account

  /**
   * A type that is defined by every action for authorization.
   * This sample uses the following trait:
   */
  type Authority = User => Boolean

  /**
   * A `ClassManifest` is used to retrieve an id from the Cache API.
   * Use something like this:
   */
  val idTag: ClassTag[Id] = classTag[Id]
  // for version 0.5 as follows
  // val idManifest: ClassManifest[Id] = classManifest[Id]

  /**
   * The session timeout in seconds
   */
  val sessionTimeoutInSeconds: Int = 3600

  /**
   * A function that returns a `User` object from an `Id`.
   * You can alter the procedure to suit your application.
   */
  def resolveUser(id: Id): Option[User] = Account.findByEmail(id)

  /**
   * Where to redirect the user after a successful login.
   */
  def loginSucceeded(request: RequestHeader): Result = {
    Logger.debug("Authentication succeeded")
    val uri = request.session.get("access_uri").getOrElse(routes.Application.index.url.toString)
    request.session - "access_uri"
    Logger.debug("Login succeeded. Redirecting to uri " + uri)
    Redirect(uri)
  }

  /**
   * Where to redirect the user after logging out
   */
  def logoutSucceeded(request: RequestHeader): Result = { 
    val uri = request.session.get("access_uri").getOrElse(routes.Message.main.url.toString)
    Redirect(uri).withSession(request.session - "access_uri")
  }
  
  /**
   * If the user is not logged in and tries to access a protected resource then redirct them as follows:
   */
  def authenticationFailed(request: RequestHeader): Result = 
    Redirect(routes.Application.index).withSession("access_uri" -> request.uri)

  /**
   * If authorization failed (usually incorrect password) redirect the user as follows:
   */
  def authorizationFailed(request: RequestHeader): Result = Forbidden("no permission")

  /**
   * A function that determines what `Authority` a user has.
   * You should alter this procedure to suit your application.
   */
  def authorize(user: User, authority: Authority): Boolean = authority(user)
  
  def authorizeAlways(user: User) = true
  
  def authorizeAdmin(user: User) = user.permission.isAdmin
  
  def authorizeNormalUser(user: User) = user.permission.isNormalUserOrAdmin

  /**
   * Whether use the secure option or not use it in the cookie.
   * However default is false, I strongly recommend using true in a production.
   */
  override lazy val cookieSecureOption: Boolean = play.api.Play.current.configuration.getBoolean("auth.cookie.secure").getOrElse(true)

  //override lazy val idContainer: IdContainer[Id] = new CookieIdContainer[Id]
}