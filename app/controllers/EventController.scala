package controllers

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc._
import models.Event
import org.joda.time.DateTime
import anorm.Pk
import anorm.NotAssigned

/**
  * Controller of event related actions.
  * User: Bartosz Jankiewicz
  * Date: 1/20/13
  * Time: 9:07 PM
  */
object EventController extends BaseController {

  val eventForm: Form[Event] = Form(
		  mapping(
				  "id"             -> ignored(NotAssigned:Pk[Long]),
				  "name"           -> nonEmptyText,
				  "description"    -> text(minLength = 10, maxLength = 5000),
				  "startDate"      -> jodaDate("yyyy-MM-dd"),
				  "eventTypeId"    -> longNumber,
				  "venueId"        -> longNumber,
				  "active"         -> boolean,
				  "date"           -> ignored(DateTime.now))(Event.apply)(e => {
					  if (e eq null) None
					  else Some((e.id, e.name, e.description, e.startDate, e.eventTypeId, e.venueId, e.active, e.created))
				  }))
  
  def venues = List("1" -> "Stadion narodowy", "2" -> "Stadion Maslice")
  
  def list = Action { implicit request =>
    Ok(views.html.event.list(
        //Event.findByStartDate(startDate, endDate)
        Nil
    ))
  }

  def update(id: Long) = Action { implicit request =>
    eventForm.bindFromRequest.fold(
      errors => BadRequest(views.html.event.form(id, errors, venues toMap)),
      event => {
        val updated = Event.update(id, event)
    	Ok(views.html.event.summary(updated))
      }
	)
  }

  def create = Action { implicit request =>
    Ok(views.html.event.form(-1, eventForm.fill(Event.createNew), venues toMap))
  }

  def edit(id: Long) = Action { implicit request =>
    Event.findById(id) match {
      case Some(existing) => Ok(views.html.event.form( existing.id.get, eventForm.fill(existing), venues toMap ))
      case None           => Redirect(routes.EventController.list) flashing "message" -> "Event could not be found"
    }
  }
  
  def find = Action{ implicit request =>
    Ok(views.html.event.list(
        Event.findByStartDate(
            DateTime.parse("2012-12-10"), 
            DateTime.parse("2012-12-30")
        )
    ))
  }

}