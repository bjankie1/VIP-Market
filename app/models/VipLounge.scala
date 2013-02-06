package models

import org.joda.time.DateTime
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import play.api.Logger

/**
 * VIP Lounge available at given sport venue. List of available lounges is being defined for
 * given event. This information serves as a template 
 */
case class VipLounge(
    id:           Pk[Long], 
    name:         String,
    venueId:      Long, 
    locationCode: String, 
    description:  String, 
    seatsNumber:  Int,
    active:       Boolean,
    created:      DateTime)

case class VipLoungePhoto(
    vipLoungeId: Long,
    photoId: Long
)

object VipLounge extends AbstractModel {
  
  def createVipLounge(venueId: Long) = {
    VipLounge(
      NotAssigned,
      "",
      venueId,
      "",
      "",
      0,
      false,
      DateTime.now()
    )
  }
  
  def simple: RowParser[VipLounge] = {
    get[Pk[Long]]("vip_lounge.id") ~
    str("vip_lounge.name") ~
    long("vip_lounge.venue_id") ~
    str("vip_lounge.location_code") ~
    str("vip_lounge.description") ~
    int("vip_lounge.seats_number") ~
    get[Boolean]("vip_lounge.active") ~
    get[DateTime]("vip_lounge.date_created") map {
      case id ~ name ~ venueId ~ locationCode ~ description ~ seats ~ active ~ dateCreated => VipLounge(
        id, name, venueId, locationCode, description, seats, active, dateCreated
      )
    }
  }
  
  def findById(id: Long): Option[VipLounge] = {
    Logger.debug(s"Loading VIP Lounge ${id}")
    val sql = "select * from vip_lounge where id = {id}"
    Logger("sql").debug(sql)
    DB.withConnection {
      (implicit connection =>
        SQL(sql).on(
          'id -> id
        ).as(simple.singleOpt)
      )
    }
  }
  
  def findByVenue(venueId: Long) = {
    Logger.debug(s"Loading VIP Lounge for venue ${venueId}")
    val sql = "select * from vip_lounge where venue_id = {venueId}"
    Logger("sql").debug(sql)
    DB.withConnection {
      (implicit connection =>
        SQL(sql).on(
          'venueId -> venueId
        ).as(simple *)
      )
    }    
  }
  
  def activate(id: Long, active: Boolean) = {
    Logger.info(s"Activating VIP lounge ${id}")
    val sql = """
              update vip_lounge set 
                active        = {active} 
              where id        = {id}
              """
    Logger("sql").debug(sql)
    DB.withConnection {
      (implicit connection =>
        SQL(sql).on(
          'id            -> id,
          'active        -> active
        ).executeUpdate
      )
    }    
    Logger.info(s"VIP lounge ${id} active set to ${active}")
  }
  
  def insert(vipLounge: VipLounge) = {
    Logger.debug(s"Inserting VIP Lounge ${vipLounge}")
    val sql = """
              insert into vip_lounge(name, venue_id, description, location_code, seats_number, active, date_created) 
              values({name}, {venueId}, {description}, {locationCode}, {seatsNumber}, {active}, {dateCreated})
              """
    Logger("sql").debug(sql)
    DB.withConnection {
      (implicit connection =>
        SQL(sql).on(
          'venueId       -> vipLounge.venueId,
          'name          -> vipLounge.name,
          'description   -> vipLounge.description,
          'locationCode  -> vipLounge.locationCode,
          'seatsNumber   -> vipLounge.seatsNumber,
          'active        -> vipLounge.active,
          'dateCreated   -> vipLounge.created
        ).executeInsert() match {
          case Some(id) => id
          case None     => throw new Exception("")
        }
      )
    }    
  }
  
  def update(id: Long, vipLounge: VipLounge) = {
    Logger.debug(s"Updating VIP Lounge ${vipLounge}")
    val sql = """
              update vip_lounge set 
                name          = {name}, 
                venue_id      = {venueId}, 
                description   = {description}, 
                location_code = {locationCode}, 
                seats_number  = {seatsNumber},
                active        = {active} 
              where id        = {id}
              """
    Logger("sql").debug(sql)
    DB.withConnection {
      (implicit connection =>
        SQL(sql).on(
          'id            -> id,
          'venueId       -> vipLounge.venueId,
          'name          -> vipLounge.name,
          'description   -> vipLounge.description,
          'locationCode  -> vipLounge.locationCode,
          'seatsNumber   -> vipLounge.seatsNumber,
          'active        -> vipLounge.active
        ).executeUpdate
      )
    }    
    Logger.info(s"VIP lounge ${id} updated successfylly")
  }
    
}