package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import org.joda.time.DateTime

/**
  * Special information related to given VipLounge usually related to its availability.
  */
case class VipLoungeExclusion(
  id: Pk[Long],
  loungeId: Long,
  note: String,
  dateStart: DateTime,
  dateEnd: DateTime
)

object VipLoungeExclusion extends AbstractModel {
  
  def simple = {
    get[Pk[Long]]("id") ~
    get[Long]("vip_lounge_id") ~
    get[String]("note") ~
    get[DateTime]("date_start") ~
    get[DateTime]("date_end") map {
      case id ~ loungeId ~ notes ~ dateStart ~ dateEnd => VipLoungeExclusion(
        id, loungeId, notes, dateStart, dateEnd)
    }
  }
  
}