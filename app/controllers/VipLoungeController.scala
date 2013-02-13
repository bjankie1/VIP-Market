package controllers

import org.joda.time.DateTime
import anorm.NotAssigned
import anorm.Pk
import models.Venue
import models.VipLounge
import play.api._
import play.api.i18n._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc._
import play.api.libs.json.Json
import play.api.libs.Files.TemporaryFile

object VipLoungeController extends BaseController {
  
  val form: Form[VipLounge] = Form(
    mapping(
      "id"          -> ignored(NotAssigned:Pk[Long]),
      "name"        -> nonEmptyText(1, 255),
      "locationCode"-> text(1, 255),
      "description" -> nonEmptyText,
      "active"      -> boolean,
      "venueId"     -> number,
      "seatsNumber" -> number
    )((id, name, locationCode, description, active, venueId, seats) => 
      VipLounge(id, name, venueId, locationCode, description, seats, active, DateTime.now()))( 
          vipLounge => {
            if( vipLounge eq null) None
            else Some((
                vipLounge.id, 
                vipLounge.name, 
                vipLounge.locationCode, 
                vipLounge.description, 
                vipLounge.active, 
                vipLounge.venueId.intValue,
                vipLounge.seatsNumber))
      }
    )
  )

  def list(venueId: Long) = Action { implicit request =>
    Logger("controller").info("Displaying vipLounges")
    val vipLounges = VipLounge.findByVenue(venueId)
    Logger("controller").info(s"loaded ${vipLounges.size} vip lounges for venue ${venueId}")
    Ok(views.html.admin.viplounge.list(venueId, vipLounges))
  }

  def edit(id: Long) = Action { implicit request =>
    Logger("controller").info(s"Editing VIP Lounge ${id}")
    VipLounge.findById(id) match {
      case Some(existing) => Ok(views.html.admin.viplounge.form( 
          existing.id.get, 
          form.fill(existing),
          Venue.getAll.map(v => v.id.get.toString -> v.name)
      ))
      case None => Redirect(routes.VenueController.list) flashing "message" -> s"VIP Lounge ${id} could not be found"
    }
  }

  def create(venueId: Long) = Action { implicit request =>
    Logger("controller").info(s"Creating VIP Lounge for venue ${venueId}")
    Ok(views.html.admin.viplounge.form( 
          -1, 
          form.fill(VipLounge.createVipLounge(venueId)),
          Venue.getAll.map(v => v.id.get.toString -> v.name)
    ))
  }
  
  def activate(vipLoungeId: Long)  = Action {
    VipLounge.activate(vipLoungeId, true)
    Logger("controller").info(s"Activated VIP Lounge ${vipLoungeId}")
    Ok(Json.obj("status" ->"OK", "message" -> s"VIP lounge activated"))
  }
  
  def disactivate(vipLoungeId: Long)  = Action {
    VipLounge.activate(vipLoungeId, false)
    Logger("controller").info(s"Disactivated VIP Lounge ${vipLoungeId}")
    Ok(Json.obj("status" ->"OK", "message" -> s"VIP lounge disactivated"))
  }
  
  def update(id: Long) = Action(parse.multipartFormData) { implicit request =>
    Logger("controller").debug(s"Updating VIP Lounge ${id}")
    form.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.viplounge.form(
          id, 
          errors, 
          Venue.getAll.map(v => v.id.get.toString ->  v.name)
        )
      ),
      vipLounge => { val updatedId = id match {
          case -1 => {
            val newId = VipLounge.insert(vipLounge)
            Logger("controller").info(s"Created VIP lounge ${newId}")
            newId
          }
          case default => {
            VipLounge.update(id, vipLounge)
            Logger("controller").info(s"Updated VIP lounge ${id}")
            id
          }
        }
        request.upload(s"vipLounge:${updatedId}")
        Logger.debug(s"Uploaded pictures for VIP lounge ${updatedId}")
      	Redirect(routes.VenueController.edit(vipLounge.venueId)).flashing("message" -> Messages("success", vipLounge.name))
      }
	)
    
  }
  
}