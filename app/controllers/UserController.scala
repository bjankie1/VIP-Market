package controllers

import models.Account
import models.NormalUser
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc._
import play.api.libs.json.Json

object UserController extends Controller {

  val userForm: Form[Account] = Form(
    mapping(
      "id" -> nonEmptyText,
      "email" -> nonEmptyText,
      "name" -> nonEmptyText,
      "password" -> nonEmptyText
    )(Account.apply)(a => {
      if( a eq null) None
      else Some((a.id, a.email, a.name, a.password))
      })
    )
    
  def jsonUsers = Action {
    Ok(Json.obj("values" -> Json.arr(Json.obj("id" -> 1, "text" -> "Bartek"), Json.obj("id" -> 2, "text" -> "Marek"))))
  }

  /**
    * List of all users
    */
  def list = Action {
    val users = Account.findAll
    Ok(views.html.user.index(users))
  }

  def edit(email: String) = Action {
    val user = Account.findByEmail(email)
    user match {
      case Some(existing) => Ok(views.html.user.form(userForm.fill(existing)))
      case None           => Redirect(routes.UserController.list) flashing "message" -> "User could not be found"
    }
  }

  def create = Action {
    Account.create(Account("id", "tester@gmail.com", "tester", "secret", NormalUser))
    Ok("")
  }

  def save = Action { implicit request =>
    userForm.bindFromRequest.fold(
      errors => BadRequest(views.html.user.form(errors)),
      contact => Ok(views.html.user.summary(contact))
    )
    Redirect("/home").flashing(
      "success" -> "The item has been created")
  }
}