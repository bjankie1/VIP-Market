package models

import org.joda.time.DateTime
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import play.api.Logger


/**
 * Building where sport Venues take place
 * 
 * @author Bartosz Jankiewicz
 */
case class Venue(
    id:          Pk[Long], 
    name:        String,
    description: String,
    address:     Address,
    active:      Boolean,
    created:     DateTime) {
}
    
object Venue extends AbstractModel {
  def create: Venue = Venue(NotAssigned, "", "", Address("","", Country("")), false, DateTime.now())


  // -- Parsers

  /**
    * Parse a User from a ResultSet
    */
  val simple: RowParser[Venue] = {
    get[Pk[Long]]("venue.id") ~
    str("venue.name") ~
    str("venue.description") ~
    get[DateTime]("venue.created_date") ~
    get[Boolean]("venue.active") ~
    str("venue.street") ~
    str("venue.city") ~
    str("venue.country") map {
       case id ~ name ~ description ~ createdAt ~ active ~ street ~ city ~ country => 
         Venue(id, name, description, Address(street, city, Country(country)), active, createdAt)
    }
  }

  // -- Queries
  
  /**
    * Retrieve a User from email.
    */
  def findById(id: Long): Option[Venue] = {
	Logger.info(s"finding venue $id")
	val sql = "select * from venue where id = {id}"
	Logger("sql").debug(sql) 
    DB.withConnection {
      (implicit connection =>
        SQL(sql).on(
          'id -> id).as(Venue.simple.singleOpt))
    }
  }
  
  def getAll: List[Venue] = {
	Logger.info("Loading all venues")
	val sql = "select * from venue order by name"
	Logger("sql").debug(sql) 
    DB.withConnection {
      (implicit connection =>
        SQL(sql).as(Venue.simple *)
      )
    }
  }
  
  def findByCity(city: String): List[Venue] = {
	Logger.info(s"finding venue in ${city}")
	val sql = "select * from venue where city like {city}"
	Logger("sql").debug(sql) 
    DB.withConnection {
      (implicit connection =>
        SQL(sql).on(
          'city -> city
        ).as(Venue.simple *)
      )
    }
  }
  
  def findByName(name: String): List[Venue] = {
	Logger.info(s"finding venue of name ${name}")
	val sql = "select * from venue where name like {name}"
	Logger("sql").debug(sql) 
    DB.withConnection {
      (implicit connection =>
        SQL(sql).on(
          'name -> name
        ).as(Venue.simple *)
      )
    }
  }
  
  def insert(venue: Venue) = {
    Logger.info(s"Saving new venue")
    val sql = """
              insert into venue(name, description, active, created_date, street, city, country)
              values({name},{description}, {active}, {createdAt}, {street}, {city}, {country})
              """
    Logger("sql").debug(sql)
    val id = DB.withConnection { implicit connection =>
      Logger.info(s"executing sql $sql")
      SQL(sql).on(
          'name         -> venue.name,
          'description 	-> venue.description,
          'active       -> venue.active,
          'createdAt    -> venue.created,
          'street       -> venue.address.street,
          'city         -> venue.address.city,
          'country      -> venue.address.country.name
      ).executeInsert() match {
        case Some(id) => id
        case None => throw new Exception("Failed to insert the venue")
      }
    }
    Logger.info(s"Venue ${venue.name} inserted successfully")
    id
  }
  
  def update(id: Long, venue: Venue) = {
    Logger.info(s"Updating venue: {id}")
    val sql = """
              update venue set name = {name}, description = {description}, active = {active}
              where id = {id}
              """
    Logger("sql") debug(sql)
    DB.withConnection{ implicit connection =>
      SQL(sql).on(
          'id           -> id,
          'name         -> venue.name,
          'description 	-> venue.description,
          'active       -> venue.active,
          'street       -> venue.address.street,
          'city         -> venue.address.city,
          'country      -> venue.address.country.name
      ).executeUpdate
    }
    Logger.info(s"Venue ${id} updated successfylly")
  }
  
}