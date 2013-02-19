package controllers

import play.api._
import play.api.mvc._
import jp.t2v.lab.play20.auth.Auth
import models.NormalUser
import models.Administrator

object Message extends Controller with Auth with AuthConfigImpl {

  // The `authorizedAction` method
  //    takes `Authority` as the first argument and
  //    a function signature `User => Request[AnyContent] => Result` as the second argument and
  //    returns an `Action`

  def main = authorizedAction(authorizeAlways) { user => implicit request =>
    val title = "message main"
    Ok(views.html.message.main(title)(views.html.main.apply(title)))
  }

  def list = authorizedAction(authorizeAlways) { user => implicit request =>
    val title = "all messages"
    Ok(views.html.message.list(title)(views.html.main.apply(title)))
  }

  def detail(id: Int) = authorizedAction(authorizeAlways) { user => implicit request =>
    val title = "messages detail "
    Ok(views.html.message.detail(title + id)(views.html.main.apply(title)))
  }

  // Only Administrator can execute this action.
  def write = authorizedAction(authorizeAdmin) { user => implicit request =>
    val title = "write message"
    Ok(views.html.message.write(title)(views.html.main.apply(title)))
  }

}