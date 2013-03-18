package controllers

import models.Account
import models.NormalUser
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.libs.json.Json
import play.api.data.validation.Constraint
import play.api.data.validation.Valid
import play.api.data.validation.Invalid
import play.api.i18n.Messages

object UserController extends BaseController {

  case class SignupData(
                         email: String,
                         name: String,
                         password: String,
                         passwordRepeat: String,
                         acceptRules: Boolean,
                         acceptNotifications: Boolean)

  val userProfileForm: Form[Account] = Form(
    mapping(
      "email" -> nonEmptyText,
      "name" -> nonEmptyText
    )(Account.apply)(a => {
      if (a eq null) None
      else Some((a.email, a.name))
    })
  )

  private def verifyPasswords = Constraint[SignupData] {
    data: SignupData =>
      Logger.debug("verifying passwords")
      data.password match {
        case data.passwordRepeat => Valid
        case _ => {
          Logger.warn("Invalid second password")
          Invalid(Messages("user.password.validation.no.match"))
        }
      }
  }

  val signupForm: Form[SignupData] = Form(
    mapping(
      "email" -> email,
      "name" -> nonEmptyText(3, 50),
      "password" -> nonEmptyText,
      "password-repeat" -> nonEmptyText,
      "agreement1" -> checked("user.validation.rules.must.agree"),
      "agreement2" -> boolean
    )(SignupData.apply)(SignupData.unapply) verifying (verifyPasswords)
  )

  /**
   * Looking for user names matching given pattern
   */
  def jsonUsers = Action {
    implicit request =>
      if (request.queryString.contains("q")) {
        val q = request.queryString("q").head
        Logger.debug(s"Looking for users starting with '$q'")
        val list = Account.idNameOnly(s"%$q%")
        val jsonObjects = list.map(idName => Json.obj("id" -> idName._1, "text" -> idName._2))
        Ok(Json.obj("values" -> jsonObjects))
      } else {
        BadRequest("missing required parameter")
      }
  }


  /**
   * Looking for user names matching given pattern
   */
  def jsonUserName = Action {
    implicit request =>
      if (request.queryString.contains("q")) {
        val q = request.queryString("q").head
        Logger.debug(s"Looking for user with ID '$q'")
        val userOpt = Account.findById(q.toLong)
        userOpt match {
          case Some(user) => {
            val jsonObjects = Json.obj("name" -> user.name)
            Ok(jsonObjects)
          }
          case None       => NotFound
        }
      } else {
        BadRequest("missing required parameter")
      }
  }

  /**
   * List of all users
   */
  def list = authorizedAction(authorizeAdmin) {
    user => implicit request =>
      val users = Account.findAll
      Ok(views.html.admin.user.list(users))
  }

  def edit(email: String) = authorizedAction(authorizeAdmin) {
    user => request =>
      val user = Account.findByEmail(email)
      user match {
        case Some(existing) => Ok(views.html.user.form(userProfileForm.fill(existing)))
        case None => Redirect(routes.UserController.list) flashing "message" -> "User could not be found"
      }
  }

  def signup = Action {
    implicit request =>
      Ok(views.html.user.signup(signupForm.fill(null)))
  }

  def signupSave = Action {
    implicit request =>
      Logger.debug("signing up a new user")
      signupForm.bindFromRequest.fold(
        errors => BadRequest(views.html.user.signup(errors)),
        success => {
          Logger.debug("Registration form successfully bound")
          Account.create(success.email, success.name, success.password, NormalUser)
          Redirect(routes.Application.index).flashing("message" -> Messages("account.created"))
        }
      )

  }

  def save = authorizedAction(authorizeAdmin) {
    user => implicit request =>
      userProfileForm.bindFromRequest.fold(
        errors => BadRequest(views.html.user.form(errors)),
        contact => Ok(views.html.user.summary(contact))
      )
      Redirect(routes.Application.index).flashing(
        "success" -> "The item has been created")
  }
}