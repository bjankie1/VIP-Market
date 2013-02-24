package controllers

import play.api.data.Forms._
import play.api._
import i18n.Messages
import play.api.data._
import play.api.mvc._
import models.Venue
import org.joda.time.DateTime
import anorm.Pk
import anorm.NotAssigned
import models.Address
import models.Country
import java.io.StringReader
import play.api.libs.iteratee.Enumerator.Pushee
import com.monochromeroad.play.xwiki.rendering.plugin.{DefaultXWikiStringStreamRenderer => XCM}
import play.api.libs.iteratee.Enumerator

object VenueController extends BaseController {


  case class Test(name: String, email: List[String])

  val testFrom: Form[Test] = Form(
    mapping(
      "name" -> nonEmptyText,
      "emails" -> Forms.list(nonEmptyText)
    )(Test.apply)(Test.unapply)
  )


  val form: Form[Venue] = Form(
    mapping(
      "id" -> ignored(NotAssigned: Pk[Long]),
      "name" -> nonEmptyText(1, 255),
      "description" -> nonEmptyText,
      "active" -> boolean,
      "street" -> nonEmptyText,
      "city" -> nonEmptyText,
      "country" -> nonEmptyText
    )((id, name, description, active, street, city, country) =>
      Venue(id, name, description, Address(street, city, Country(country)), active, DateTime.now()))(
      venue => {
        if (venue eq null) None
        else Some((
          venue.id,
          venue.name,
          venue.description,
          venue.active,
          venue.address.street,
          venue.address.city,
          venue.address.country.name))
      }
    )
  )

  def list = authorizedAction(authorizeAdmin) { user => implicit request =>
      val venues = Venue.getAll
      Logger("controller").info(s"loaded ${venues.size} venues")
      Ok(views.html.admin.venue.list(venues))
  }

  def edit(id: Long) = authorizedAction(authorizeAdmin) { user => implicit request =>
      Venue.findById(id) match {
        case Some(existing) => Ok(views.html.admin.venue.form(existing.id.get, form.fill(existing)))
        case None => Redirect(routes.VenueController.list) flashing "message" -> "Venue could not be found"
      }
  }

  def create = authorizedAction(authorizeAdmin) { user => implicit request =>
      Ok(views.html.admin.venue.form(
        -1l,
        form.fill(Venue.create)
      ))
  }

  def update(id: Long) = Action {
    implicit request =>
      Logger("controller").debug(s"Updating venue ${id}")
      form.bindFromRequest.fold(
        errors => BadRequest(views.html.admin.venue.form(id, errors)),
        venue => {
          id match {
            case -1 => Venue.insert(venue)
            case _  => Venue.update(id, venue)
          }

          Redirect(routes.VenueController.list) flashing "message" -> Messages("save.success", venue.name)
        }
      )
  }

  def preview(id: Long) = Action {
    Venue.findById(id) match {
      case Some(existing) => Ok(views.html.admin.venue.preview(existing))
      case None => Redirect(routes.VenueController.list) flashing "message" -> "Venue could not be found"
    }

  }

  def wiki = Action {
    val src = new StringReader("**TEST** {{comment}}This is a comment that would not be contained in the result{{/comment}}")
    // <p><strong>TEST</strong>

    val onStart: Pushee[String] => Unit = {
      pushee =>
        val push: String => Unit = {
          p1 =>
            pushee.push(p1)
        }
        XCM.render(src, push)
        pushee.close()
    }

    val channel = Enumerator.pushee(onStart)
    Ok.stream(channel).as(JSON)
  }

}