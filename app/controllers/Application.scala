package controllers

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.Play.current
import play.api.data.validation.Constraints._

import models.Account

object Application  extends Controller  {

  val loginForm = Form {
    mapping(
        "email" -> email, 
        "password" -> text)(Account.authenticate)(_.map(u => (u.email, "")))
      .verifying("Invalid email or password", result => result.isDefined)
  }

  def index = Action {
    Ok(views.html.index("SportsMarket.pl"))
  }

  def logout = Action { implicit request =>
    Ok("You've been logged out")
  }

  def authenticate = Action { implicit request =>
    Ok("done")
  }

}