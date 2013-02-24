package controllers

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import models.Account
import jp.t2v.lab.play20.auth.{LoginLogout, Auth}

object Application extends Controller with LoginLogout with Auth with AuthConfigImpl {

  val loginForm = Form {
    mapping(
        "email" -> email, 
        "password" -> text)(Account.authenticate)(_.map(u => (u.email, "")))
      .verifying("Invalid email or password", result => result.isDefined)
  }

  def index = optionalUserAction{ implicit maybeUser => request =>
    Ok(views.html.index(maybeUser))
  }

  def login = Action { implicit request =>
    Ok(views.html.login())
  }

  /**
   * Return the `gotoLogoutSucceeded` method's result in the logout action.
   *
   * Since the `gotoLogoutSucceeded` returns `PlainResult`, 
   * you can add a procedure like the following.
   * 
   *   gotoLogoutSucceeded.flashing(
   *     "success" -> "You've been logged out"
   *   )
   */
  def logout = Action { implicit request =>
    gotoLogoutSucceeded
  }

  def authenticate = Action { implicit request =>
    Logger.info(s"authenticating request")
    loginForm.bindFromRequest.fold(
      formWithErrors => {
        Logger.warn(s"Authentication failed ${formWithErrors.errors.foldLeft("::")((a,b) => a + b.message)}")
        BadRequest(views.html.login())
      },
      user => {
        Logger.info("bound login form values")
        gotoLoginSucceeded(user.get.email)
      }
    )
  }

}