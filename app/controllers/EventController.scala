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
 * Controller of event related actions.
 * User: Bartosz Jankiewicz
 * Date: 1/20/13
 * Time: 9:07 PM
 */
object EventController extends BaseController {

  val eventForm: Form[(Event,String)] = Form(
    mapping(
      "id" -> ignored(NotAssigned: Pk[Long]),
      "name" -> nonEmptyText,
      "description" -> text(minLength = 10, maxLength = 5000),
      "startDate" -> jodaDate("yyyy-MM-dd hh:mm").verifying(Messages("validation.date.must.be.future"), _.isAfterNow),
      "eventTypeId" -> longNumber,
      "venueId" -> longNumber,
      "active" -> boolean,
      "date" -> ignored(DateTime.now),
      "approvers" -> text.verifying("event.select.approvers.invalid", a => a.split(",").forall(_.matches("\\d+")))
    )((id, name, description, startDate, eventTypeId, venueId, active, date, approvers) =>
        (Event(id, name, description, startDate, eventTypeId, venueId, active, date), approvers))(e => {
          if (e eq null) None
          else Some((e._1.id, e._1.name, e._1.description, e._1.startDate, e._1.eventTypeId, e._1.venueId, e._1.active, e._1.created,
            e._2))
    })
  )

  case class VipLoungesAtEvent(lounges: List[VipLoungeAtEvent])

  val vipLoungesForm: Form[VipLoungesAtEvent] = Form(
    mapping(
      "lounges"    -> Forms.list(mapping(
        "eventId"     -> longNumber,
        "vipLoungeId" -> longNumber,
        "basePrice"   -> bigDecimal,
        "active"      -> boolean
      )(VipLoungeAtEvent.apply)(VipLoungeAtEvent.unapply))
    )(VipLoungesAtEvent.apply)(VipLoungesAtEvent.unapply)
  )

  def venues = Venue.getAll.map(v => (v.id.get.toString, v.name))

  def eventTypes = EventType.findAll map( et => (et.id.get.toString, et.name))

  /**
   * Id for file owner attribute generated for given event
   * @param eventId Event id to generate owner id for
   * @return Returns id of file owner
   */
  def fileOwnerId(eventId: Long) = s"event:${eventId}"

  /**
   * Display the first page with events by default
   */
  def index: Action[(play.api.mvc.AnyContent, controllers.EventController.User)] = list(1)

  /**
   * List of events sorted by date. There are 20 events returned and the output is pageable
   */
  def list(page: Int = 1) = authorizedAction(authorizeAdmin) { user => implicit request =>
      Ok(views.html.admin.event.list(
        Event.listByPage(page, 20),
        Event.count
      ))
  }

  def create = Action {
    implicit request =>
      Ok(views.html.admin.event.form(
        -1,
        eventForm.fill( (Event.createNew, "")),
        venues,
        eventTypes
      ))
  }

  /**
   * Edit event action.
   * @param id Identifier of event
   * @return Returns related action
   */
  def edit(id: Long) = Action {
    implicit request =>
      Logger.debug(s"Editing event ${id}")
      val approvers = EventApprover.approversForEvent(id)
      Logger.debug(s"approvers $approvers")
      val approverIds = approvers match {
        case Nil => ""
        case _   => approvers.map(_.userId.toString).mkString(",")
      }

      Event.findById(id) match {
        case Some(existing) => Ok(views.html.admin.event.form(
          existing.id.get,
          eventForm.fill((existing, approverIds)),
          venues,
          eventTypes
        ))
        case None => Redirect(routes.EventController.list(1)) flashing "message" -> "Event could not be found"
      }
  }

  /**
   * Update given event loaded from form data
   */
  def update(id: Long) = Action(parse.multipartFormData) {
    implicit request =>
      eventForm.bindFromRequest.fold(
        errors => BadRequest(views.html.admin.event.form(
          id,
          errors,
          venues,
          eventTypes
        )),
        event => {
          val eventId = id match {
            case -1l => Event.insert(event._1)
            case _   => {
              Event.update(id, event._1)
              id
            }
          }
          EventApprover.replace(eventId, event._2.split(",").map(_.toLong))
          request.upload(fileOwnerId(eventId))
          Redirect(routes.EventController.index) flashing "message" -> Messages("save.success", event._1.name)
        }
      )
  }

  /**
   * Display form presenting available VIP lounges for given event. At this point it's possible to create list of all
   * lounges available and create corresponding VipLoungeAtEvent objects. 
   */
  def vipLounges(id: Long) = Action {
    implicit request =>
      val event = Event.findById(id)
      event match {
        case None => NotFound
        case Some(existingEvent) => {
          val vle = VipLoungeAtEvent.findLoungesAtEvent(id)
          val vleFinal = vle match {
            case Nil => {
              //no lounges initialized
              val lounges = VipLounge.findByVenue(existingEvent.venueId)
              lounges.map(l => VipLoungeAtEvent(id, l.id.get, l.basePrice, false))
            }
            case default => vle
          }
          Logger.debug(s"Showing options for ${vle.size} lounges")
          val lounges = VipLoungesAtEvent(vleFinal)
          Ok(views.html.admin.event.viplounges(id, vipLoungesForm.fill(lounges), VipLounge.idToName(existingEvent.venueId)))
        }
      }
  }

  def updateLounges(id: Long) = Action {
    implicit request =>
      Logger.info(s"Executing update of available lounges for event ${id}")
      Event.findById(id) match {
        case None => {
          Logger.warn(s"No event of ID = ${id}")
          NotFound //no such event by given ID
        }
        case Some(existing) => {
          vipLoungesForm.bindFromRequest.fold(
            errors => {
              Logger.warn(s"Error binding lounges for ${id}")
              BadRequest(views.html.admin.event.viplounges(id, errors, VipLounge.idToName(existing.id.get)))
            },
            lounges => {
              lounges.lounges.map(vle =>
                VipLoungeAtEvent.update(vle)
              )
              Logger.debug(s"Redirecting to ${routes.EventController.list(1).url} after successful action")
              Redirect(routes.EventController.list(1).url)
            })
        }
      }
  }

  def photos(eventId: Long) = FileController.editFiles(fileOwnerId(eventId))

  def find = TODO

  def activate(id: Long) = Action {
    implicit request =>
      Event.activate(id, true)
      Ok("aktywowana impreza")
  }

  def disactivate(id: Long) = Action {
    implicit request =>
      Event.activate(id, false)
      Ok("wstrzymana impreza")
  }

}