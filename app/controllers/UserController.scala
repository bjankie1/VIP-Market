package controllers

import models.Account
import models.NormalUser
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc._
import play.api.libs.json.Json
import anorm.NotAssigned
import play.api.data.validation.Constraint
import play.api.data.validation.Valid
import play.api.data.validation.Invalid
import play.api.i18n.Messages

object UserController extends Controller {

  val userForm: Form[Account] = Form(
    mapping(
      "email" -> nonEmptyText,
      "name"  -> nonEmptyText,
      "password" -> nonEmptyText
    )(Account.apply)(a => {
      if( a eq null) None
      else Some((a.email, a.name, a.password))
      })
    )
  
  case class SignupData(
      email: String, 
      password: String, 
      passwordRepeat: String, 
      acceptRules: Boolean, 
      acceptNotifications: Boolean)
  
  private def verifyPasswords = Constraint[SignupData] { data: SignupData =>
    Logger.debug("verifying passwords")
    data.password match {
      case data.passwordRepeat 	=> Valid
      case _                    => {
        Logger.warn("Invalid second password")
        Invalid(Messages("user.password.validation.notmatch"))
      }
    }
  }
    
  val signupForm: Form[SignupData] = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText,
      "password-repeat" -> nonEmptyText,
      "agreement1" -> checked(Messages("user.validation.rules.must.agree")),
      "agreement2" -> checked("user.validation.notifications.must.agree")
    )(SignupData.apply)(SignupData.unapply) verifying(verifyPasswords)
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

  def signup = Action { implicit request =>
    Ok(views.html.user.signup(signupForm.fill(null)))
  }
  
  def signupSave = Action { implicit request =>
    Logger.debug("signing up a new user")
    signupForm.bindFromRequest.fold(
      errors  => BadRequest(views.html.user.signup(errors)),
      success => {
        Logger.debug("Registration form successfully bound")
        
        Redirect(routes.Application.index).flashing("message" -> Messages("account.created"))
      }
    )
    
  }

  def save = Action { implicit request =>
    userForm.bindFromRequest.fold(
      errors => BadRequest(views.html.user.form(errors)),
      contact => Ok(views.html.user.summary(contact))
    )
    Redirect(routes.Application.index).flashing(
      "success" -> "The item has been created")
  }
}