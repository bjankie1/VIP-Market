package controllers

import play.api._
import i18n.Messages
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import models._
import org.joda.time.DateTime
import anorm.Pk
import anorm.NotAssigned
import models.VipLoungeAtEvent

/**
 * Controller of event type related actions.
 * User: Bartosz Jankiewicz
 * Date: 2/22/13
 * Time: 9:07 PM
 */
object EventTypeController extends BaseController {

  val eventTypeForm: Form[EventType] = Form(
    mapping(
      "id" -> ignored(NotAssigned: Pk[Long]),
      "name" -> nonEmptyText
    )(EventType.apply)(EventType.unapply)
  )

  /**
   * List of events sorted by date. There are 20 events returned and the output is pageable
   */
  def list = authorizedAction(authorizeAdmin) { user => implicit request =>
      Ok(views.html.admin.eventtype.list(EventType.findAll))
  }

  /**
   * Update given event type loaded from form data
   */
  def update(id: Long) = Action {
    implicit request =>
      eventTypeForm.bindFromRequest.fold(
        errors => BadRequest(views.html.admin.eventtype.form(id, errors)),
        eventType => {
          id match {
            case -1 => EventType.insert(eventType)
            case _  => EventType.update(id, eventType)
          }
          Redirect(routes.EventTypeController.list) flashing "message" -> Messages("save.success", eventType.name)
        }
      )
  }

  def create = Action {
    implicit request =>
      Logger.debug("Creating new event type")
      Ok(views.html.admin.eventtype.form(
        -1,
        eventTypeForm.fill(EventType(NotAssigned,"")))
      )
  }

  def edit(id: Long) = Action {
    implicit request =>
      Logger.debug(s"Editing event type ${id}")
      EventType.findById(id) match {
        case Some(eventType) => Ok(views.html.admin.eventtype.form(
          eventType.id.get, eventTypeForm.fill(eventType)))
        case None => Redirect(routes.EventController.list(1)) flashing "message" -> Messages("not.found")
      }
  }

}