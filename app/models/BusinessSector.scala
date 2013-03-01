package models

import play.api.db._
import anorm._
import anorm.SqlParser._
import play.api.Logger

/**
 * Defines seats available within given sector of venue.
 * User: fox
 * Date: 3/1/13
 * Time: 7:44 PM
 */
case class BusinessSector(id: String, venueId: Long, rowScheme: DisplayScheme.DisplayScheme)

object DisplayScheme extends Enumeration {

  type DisplayScheme = Value

  val Numeric, Letter, Roman = Value
}

case class BusinessSectorRow( row: Int, seats: Int)

object BusinessSector {

  // ~parser
  def simple: RowParser[BusinessSector] = {
    str("sector.id") ~
    long("sector.venue_id") ~
    str("sector.display_scheme") map {
      case id ~ venueId ~ displayScheme => BusinessSector(id, venueId, DisplayScheme.withName(displayScheme))
    }
  }

  // ~queries

  def sectorsForVenue(venueId: Long) = {
    Logger.debug(s"Loading sectors for venue $venueId")
    val sql = "select * from business_sector where venue_id = {venueId}"
    Logger("sql").debug(sql)

    DB.withConnection {
      implicit connection =>
        SQL(sql).on(
          'venueId -> venueId
      ).as(simple *)
    }
  }

  def insert(sector: BusinessSector) = {
    Logger.debug(s"Storing sector ${sector.id} for ${sector.venueId}")
    val sql =
      """
        |insert into business_sector(id, venue_id, display_scheme)
        |values({id}, {venueId}, {displayScheme}
      """.stripMargin
    Logger("sql").debug(sql)
    DB.withConnection { implicit connection =>
      SQL(sql).on(
        'id -> sector.id,
        'venueId -> sector.venueId,
        'displayScheme -> sector.rowScheme
      ).executeInsert() match {
        case Some(id) => id
        case None     => throw new Exception("Failed storing new BusinessSector")
      }
    }
  }

  def update(sector: BusinessSector) = {
    Logger.debug(s"updating sector ${sector.id} for ${sector.venueId}")
    val sql =
      """
        |update business_sector set display_scheme = {displayScheme}
        |where id = {id} and venue_id = {venue
      """.stripMargin
    Logger("sql").debug(sql)
    DB.withConnection { implicit connection =>
      SQL(sql).on(
        'id -> sector.id,
        'venueId -> sector.venueId,
        'displayScheme -> sector.rowScheme
      ).executeUpdate()
    }
  }

}

object BusinessSectorRow {

  // ~parser
  def simple = {
    int("row.row") ~
    int("row.seats") map {
      case row ~ seats => BusinessSectorRow(row, seats)
    }
  }

  // ~queries
}