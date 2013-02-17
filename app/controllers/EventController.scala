package controllers

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc._
import models._
import org.joda.time.DateTime
import anorm.Pk
import anorm.NotAssigned
import models.VipLoungeAtEvent
import models.VipLoungeAtEvent
import views.html.defaultpages.notFound

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
            "date"           -> ignored(DateTime.now)
          )(Event.apply)(e => {
            if (e eq null) None
              else Some((e.id, e.name, e.description, e.startDate, e.eventTypeId, e.venueId, e.active, e.created))
            }
          )
  )
				  
  case class VipLoungesAtEvent(lounges: List[VipLoungeAtEvent])
  
  val vipLoungesForm: Form[VipLoungesAtEvent] = Form(
    mapping(
      "lounges" -> Forms.list(mapping(
        "eventId"     -> longNumber,
        "vipLoungeId" -> longNumber,
        "basePrice"   -> bigDecimal,
        "active"      -> boolean
      )(VipLoungeAtEvent.apply)(VipLoungeAtEvent.unapply))
    )(VipLoungesAtEvent.apply)(VipLoungesAtEvent.unapply)
  )

  def venues = Venue.getAll.map(v => (v.id.get.toString, v.name))
  
  /**
   * Display the first page with events by default
   */
  def index: Action[play.api.mvc.AnyContent] = list(1)
  
  /**
   * List of events sorted by date. There are 20 events returned and the output is pageable
   */
  def list(page: Int = 1) = Action { implicit request =>
    Ok(views.html.admin.event.list(
        Event.listByPage(page, 20),
        Event.count
    ))
  }

  /**
   * Update given event loaded from form data
   */
  def update(id: Long) = Action { implicit request =>
    eventForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.event.form(id, errors, venues)),
      event => {
        val updated = Event.update(id, event)
    	Ok(views.html.admin.event.summary(updated))
      }
	)
  }
  
  /**
   * Display form presenting available VIP lounges for given event. At this point it's possible to create list of all
   * lounges available and create corresponding VipLoungeAtEvent objects. 
   */
  def vipLounges(id: Long) = Action{ implicit request =>
    val event = Event.findById(id)
    event match {
      case None => NotFound
      case Some(existingEvent) => {
    	val vle = VipLoungeAtEvent.findLoungesAtEvent(id)
	    val vleFinal = vle match {
	      case Nil     => { //no lounges initialized
	        val lounges = VipLounge.findByVenue(existingEvent.id.get)
	        lounges.map( l => VipLoungeAtEvent(id, l.id.get, 100, true))
	      }
	      case default => vle 
	    }
	    val lounges = VipLoungesAtEvent(vleFinal)
	    Ok(views.html.admin.event.viplounges(id, vipLoungesForm.fill(lounges), VipLounge.idToName(id)))
      } 
    }
  }

  def updateLounges(id: Long) = Action { implicit request =>
    Logger.info(s"Executing update of available lounges for event ${id}")
    Event.findById(id) match {
      case None => {
        Logger.warn(s"No event of ID = ${id}")
        NotFound //no such event by given ID 
      }
      case Some(existing) => {
	    vipLoungesForm.bindFromRequest.fold(
	      errors => BadRequest(views.html.admin.event.viplounges(id, errors, VipLounge.idToName(existing.id.get))),
	      lounges => {
	        lounges.lounges.map(vle =>
	          VipLoungeAtEvent.update(vle)
	        )
	    	Redirect("/admin/event")
	      }
		)
      }
    }
  }

  def create = Action { implicit request =>
    Ok(views.html.admin.event.form(-1, eventForm.fill(Event.createNew), venues))
  }

  def edit(id: Long) = Action { implicit request =>
    Event.findById(id) match {
      case Some(existing) => Ok(views.html.admin.event.form( 
          existing.id.get, eventForm.fill(existing), venues ))
      case None           => Redirect(routes.EventController.list(1)) flashing "message" -> "Event could not be found"
    }
  }
  
  def find = Action{ implicit request =>
    Ok(views.html.admin.event.list(
        Event.findByStartDate(
            DateTime.parse("2012-12-10"), 
            DateTime.parse("2012-12-30")
        ), 1
    ))
  }
  
  def activate(id: Long) = Action { implicit request =>
    Ok("aktywowana impreza")
  }

  def disactivate(id: Long) = Action { implicit request =>
    Ok("wstrzymana impreza")
  }
}