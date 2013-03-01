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
case class BusinessSector(id: String, venueId: Int, rowScheme: DisplayScheme.DisplayScheme)

object DisplayScheme extends Enumeration {

  type DisplayScheme = Value

  val Numeric, Letter, Roman = Value
}

case class BusinessSectorRow( sectorId: String, venueId: Int, row: Int, seats: Int)

object BusinessSector {

  // ~parser
  def simple: RowParser[BusinessSector] = {
    str("sector.id") ~
    int("sector.venue_id") ~
    str("sector.display_scheme") map {
      case id ~ venueId ~ displayScheme => BusinessSector(id, venueId, DisplayScheme.withName(displayScheme))
    }
  }

  // ~queries

  def findForVenue(venueId: Long) = {
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

  // ~updates

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
    str("row.sector_id") ~
    int("row.venue_id") ~
    int("row.sector_id") ~
    int("row.venue_id") map {
      case sectorId ~ venueId ~ row ~ seats => BusinessSectorRow(sectorId, venueId, row, seats)
    }
  }

  // ~queries

  def findForVenueAndSector(venueId: Int, sector: String) = {
    Logger.debug(s"Loading rows for $sector in $venueId")
    val sql =
      """
        |select * from business_sector_row where venue_id = {venueId} and sector_id = {sectorId}
      """.stripMargin
    DB.withConnection { implicit connection =>
      SQL(sql).on(
        'venueId -> venueId,
        'sectorId -> sector
      ).as( simple *)
    }
  }

  // ~updates

  def insert(row: BusinessSectorRow) = {
    Logger.debug(s"Inserting business sector row ${row.row} of ${row.sectorId} in ${row.venueId}")
    val sql =
      """
        |insert into business_sector_row(sector_id, venue_id, row, seats)
        |values({sectorId}, {venueId}, {row}, {seats})
      """.stripMargin
    Logger("sql").debug(sql)
    DB.withConnection { implicit connection =>
      SQL(sql).on(
        'sectorId -> row.sectorId,
        'venueId -> row.venueId,
        'row -> row.row,
        'seats -> row.seats
      ).executeInsert()
    }


    def update(row: BusinessSectorRow) = {
      Logger.debug(s"Updating business sector row ${row.row} of ${row.sectorId} in ${row.venueId}")
      val sql =
        """
          |update business_sector_row set seats = {seats}
          |where sector_id = {sectorId} and venue_id = {venueId} and row =  {row}
        """.stripMargin
      Logger("sql").debug(sql)
      DB.withConnection { implicit connection =>
        SQL(sql).on(
          'sectorId -> row.sectorId,
          'venueId -> row.venueId,
          'row -> row.row,
          'seats -> row.seats
        ).executeInsert()
      }
    }
  }

}