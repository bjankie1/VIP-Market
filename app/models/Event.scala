package models

import org.joda.time.DateTime
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import play.api.Logger

/**
 * Sport event.
 * 
 * @param name Name of event
 * @param description Description of event
 * @param startDate Start date event
 * @param eventTypeId Type of the event
 * @param venueId Id of venue
 * @param active Is the event active
 * @param created Date the event object has been created
 */
case class Event(
    id: Pk[Long], 
    name: String, 
    description: String,
    startDate: DateTime, 
    eventTypeId: Long, 
    venueId: Long,
    active: Boolean,
    created: DateTime) {
  
  def withId(id: Long) = Event(Id(id), name, description, startDate, eventTypeId, venueId, active, created)
}
    
/**
 * Seats excluded from current event
 */
case class SeatsExclussion(
    id: Pk[Long],
    sector: String,
    rows: String,
    columns: String
)

object Event extends AbstractModel {
  
  def createNew = Event(
    NotAssigned,
    "",
    "",
    DateTime.now(),
    0l,
    0l,
    false,
    DateTime.now()
  )
  
  // -- Parsers

  /**
    * Parse a User from a ResultSet
    */
  val simple: RowParser[Event] = {
    get[Pk[Long]]("event.id") ~
    str("event.name") ~
    str("event.description") ~
    get[DateTime]("event.start_date") ~
    long("event.event_type_id") ~ 
    long("event.venue_id")  ~ 
    get[Boolean]("event.active")  ~ 
    get[DateTime]("event.date_created") map {
       case id ~ name ~ description ~ startDate ~ eventTypeId ~ venueId ~ active ~ created => 
         Event(id, name, description, startDate, eventTypeId, venueId, active, created)
    }
  }

  // -- Queries

  /**
    * Retrieve a User from email.
    */
  def findById(id: Long): Option[Event] = {
    Logger.info("finding event")
    val sql = "select * from event where id = {id}"
    Logger("sql").debug(sql)
    DB.withConnection {
      (implicit connection =>
        SQL(sql).on(
          'id -> id).as(Event.simple.singleOpt))
    }
  }
  
  /**
   * Retrieve a set of events by starting date range
   */
  def listByPage(page: Int, limit: Int): List[Event] = {
    Logger.info(s"Finding events from offset ${(page -1)*limit} to ${page*limit}")
    val sql = """
            select * from event order by start_date LIMIT {limit} OFFSET {offset}
            """
    Logger("sql").debug(sql)
    DB.withConnection {
      (implicit connection =>
        SQL(sql).on(
          'limit 	-> limit,
          'offset 	-> Math.max(0, page - 1) * limit
        ).as(Event.simple *))
    }
  }
  
  /**
    * Retrieve a User from email.
    */
  def count: Int = {
    Logger.info("finding event")
    val sql = "select count(*) from event"
    Logger("sql").debug(sql)
    val result = DB.withConnection {
      (implicit connection =>
        SQL(sql).as(scalar[Long].single)
      )
    }
    result.intValue()
  }  
    
  /**
   * Retrieve a set of events by starting date range
   */
  def findByStartDate(startDate: DateTime, endDate: DateTime): List[Event] = {
    Logger.info(s"Finding events between ${startDate} and ${endDate}")
    val sql = """
            select * from event where start_date between {rangeStart} and {rangeEnd}
            """
    Logger("sql").debug(sql)
    DB.withConnection {
      (implicit connection =>
        SQL(sql).on(
          'rangeStart 	-> startDate,
          'rangeEnd 	-> endDate
        ).as(Event.simple *))
    }
  }
  
  // -- Updates
  
  def insert(event: Event): Long = {
    Logger.info(s"Inserting event ${event.id}")
    val sql = """
          insert into event(name, description, start_date, event_type_id, venue_id, active, date_created)
          values({name}, {description}, {startDate}, {eventTypeId}, {venueId}, {active}, {created})
      """
    Logger("sql").debug(sql)
    DB.withConnection(implicit connection => 
      SQL(sql).on(
          'name 		-> event.name,
          'description 	-> event.description,
          'startDate 	-> event.startDate,
          'eventTypeId 	-> event.eventTypeId,
          'venueId 		-> event.venueId,
          'active       -> event.active,
          'created      -> event.created
      ).executeInsert()) match {
        case Some(id) => id
        case None => throw new Exception("Did not save Event")
    }
  }
  
  
  def update(id: Long, event: Event) = {
    Logger.info(s"Updating event ${id}")
    val sql = """
            update event set 
    	      name = {name}, description = {description}, start_date = {startDate}, 
              event_type_id = {eventTypeId}, venue_id = {venueId}
            where id = {id}
          """
    Logger("sql") debug(sql)
    DB.withConnection(implicit connection => 
      SQL(sql).on(
          'id			-> id,
          'name 		-> event.name,
          'description 	-> event.description,
          'startDate 	-> event.startDate,
          'eventTypeId 	-> event.eventTypeId,
          'venueId 		-> event.venueId
      ).executeUpdate)
    event.withId(id)
  }
  
  def activate(id: Long, active: Boolean) = {
    Logger.info(s"Setting event ${id} to active = ${active}")
    val sql = """
            update event set 
    	      active = {active}
            where id = {id}
          """
    Logger("sql") debug(sql)
    DB.withConnection(implicit connection => 
      SQL(sql).on(
          'id			-> id,
          'active 		-> active
      ).executeUpdate)
  }
  
}