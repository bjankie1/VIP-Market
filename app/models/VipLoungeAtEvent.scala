package models

import org.joda.time.DateTime
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import play.api.Logger

case class VipLoungeAtEvent(
    eventId: Long,
    loungeId: Long,
    basePrice: Double
)

object VipLoungeAtEvent {
  
  def simple = {
    get[Long]("vip_lounge_event.event_id") ~
    get[Long]("vip_lounge_event.vip_lounge_id") ~
    get[Double]("vip_lounge_event.base_price") map {
      case eventId ~ vipLoungeId ~ price => VipLoungeAtEvent(eventId, vipLoungeId, price)
    }
  }
  
  def load(eventId: Long, vipLoungeId: Long) = {
    Logger.debug(s"Loading vip lounge ${vipLoungeId} at event ${eventId}")
    val sql = """
      select * from vip_lounge_event where vip_lounge_id = {vipLoungeId} and event_id = {eventId}
      """
    Logger("sql").debug(sql)
    DB.withConnection { implicit connection =>
      SQL(sql).on(
        'vipLoungeId -> vipLoungeId,
        'eventId     -> eventId
      ).as(simple.singleOpt)
    }
  }
  
  def findLoungesAtEvent(eventId: Long) = {
    Logger.debug(s"Loading VIP lounges at ${eventId}")
    val sql = """
      select * from vip_lounge_event where event_id = {eventId}
      """
    Logger("sql").debug(sql)
    DB.withConnection { implicit connection =>
      SQL(sql).on(
        'eventId -> eventId
      ).as(simple *)
    }
  }
  
  def create(vipLoungeAtEvent: VipLoungeAtEvent) = {
    Logger.debug(s"Saving VIP lounge ${vipLoungeAtEvent.loungeId} at ${vipLoungeAtEvent.eventId}")
    val sql = """
      insert into vip_lounge_event(event_id,vip_lounge_id,base_price) values({eventId},{vipLoungeId},{price})
      """
    Logger("sql").debug(sql)
    DB.withConnection { implicit connection =>
      SQL(sql).on(
        'eventId     -> vipLoungeAtEvent.eventId,
        'vipLoungeId -> vipLoungeAtEvent.loungeId,
        'price       -> vipLoungeAtEvent.basePrice
      ).executeInsert()
    }
  }
  
  def update(vipLoungeAtEvent: VipLoungeAtEvent) = {
    Logger.debug(s"Updating VIP lounge ${vipLoungeAtEvent.loungeId} at ${vipLoungeAtEvent.eventId}")
    val sql = """
      update vip_lounge_event 
      set base_price={price}
      where event_id={eventId} and vip_lounge_id={vipLoungeId} 
      """
    Logger("sql").debug(sql)
    DB.withConnection { implicit connection =>
      SQL(sql).on(
        'eventId     -> vipLoungeAtEvent.eventId,
        'vipLoungeId -> vipLoungeAtEvent.loungeId,
        'price       -> vipLoungeAtEvent.basePrice
      ).executeUpdate()
    }
  }
  
}