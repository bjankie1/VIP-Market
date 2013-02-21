package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import play.api.Logger

case class EventType(
    id: Pk[Long],
    name: String)

object EventType {

  // ~parsers

  def simple: RowParser[EventType] = {
    get[Pk[Long]]("event.id") ~
    str("event.name") map {
      case id ~ name => EventType(id, name)
    }
  }

  def findAll: List[EventType] = {
    Logger.debug("Loading all event types")
    val sql =
      """
        |select * from event_type order by name
      """.stripMargin
    Logger("sql").debug(sql)
    DB.withConnection {
      (implicit connection =>
      SQL(sql).as(simple *)
    )}
  }

}