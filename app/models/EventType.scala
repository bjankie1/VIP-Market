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
    get[Pk[Long]]("event_type.id") ~
    str("event_type.name") map {
      case id ~ name => EventType(id, name)
    }
  }

  // ~queries

  def findById(id: Long) = {
    Logger.debug(s"finding EventType ${id}")
    val sql =
      """
        |select * from event_type where id = {id}
      """.stripMargin
    Logger("sql").debug(sql)
    DB.withConnection(implicit connection =>
      SQL(sql).on(
        'id -> id
      ).as(simple.singleOpt)
    )
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


  // ~updates
  def insert( et: EventType) = {
    Logger.debug(s"Inserting new event type ${et.name}")
    val sql =
      """
        |insert into event_type(name)
        |values({name})
      """.stripMargin
    Logger("sql").debug(sql)
    DB.withTransaction( implicit connection =>
      SQL(sql).on(
        'name -> et.name
      ).executeInsert()) match {
        case None => throw new Exception("Did not save event type")
        case Some(id) => id
      }
  }

  def update(id: Long, et: EventType) = {
    Logger.debug(s"Updating event type ${id}")
    val sql =
      """
        |update event_type
        |set name = {name}
        |where id = {id}
      """.stripMargin
    Logger("sql").debug(sql)
    DB.withConnection(implicit connection =>
      SQL(sql).on(
        'name -> et.name,
        'id   -> id
      ).executeUpdate()
    )
  }
}