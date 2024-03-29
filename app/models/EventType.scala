package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import play.api.Logger

case class EventType(
    id: Pk[Int],
    name: String)

object EventType {

  val TABLE_NAME = "event_type"

  // ~parsers

  def simple: RowParser[EventType] = {
    get[Pk[Int]](s"$TABLE_NAME.id") ~
    str(s"$TABLE_NAME.name") map {
      case id ~ name => EventType(id, name)
    }
  }

  // ~queries

  def findById(id: Long) = {
    Logger.debug(s"finding EventType $id")
    val sql =
      s"""
        |select * from $TABLE_NAME where id = {id}
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
      s"""
        |select * from $TABLE_NAME order by name
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
      s"""
        |insert into $TABLE_NAME(name)
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
      s"""
        |update $TABLE_NAME
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