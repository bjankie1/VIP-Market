package models

import play.api.Play.current
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

case class BusinessSectorRow( sectorId: String, venueId: Long, row: Int, seats: Int)

object BusinessSector {

  // ~parser
  def simple: RowParser[BusinessSector] = {
    str("business_sector.id") ~
    long("business_sector.venue_id") ~
    str("business_sector.row_scheme") map {
      case id ~ venueId ~ displayScheme => BusinessSector(id, venueId, DisplayScheme.withName(displayScheme))
    }
  }

  // ~queries

  def findForVenue(venueId: Long) = {
    Logger.debug(s"Loading sectors for venue $venueId")
    val sql =
      """
        | select *
        | from business_sector
        | where venue_id = {venueId}
      """.stripMargin
    Logger("sql").debug(sql)

    DB.withConnection {
      implicit connection =>
        SQL(sql).on(
          'venueId -> venueId
      ).as(simple *)
    }
  }


  def findSector(venueId: Long, id: String) = {
    Logger.debug(s"Loading sector $id for venue $venueId")
    val sql =
      """
        | select *
        | from business_sector
        | where venue_id = {venueId} and id={id}
      """.stripMargin
    Logger("sql").debug(sql)

    DB.withConnection {
      implicit connection =>
        SQL(sql).on(
          'venueId -> venueId,
          'id      -> id
        ).as(simple singleOpt)
    }
  }


  // ~updates
  
  def insertOrUpdate(sector: BusinessSector) = {
    update(sector) match {
      case 0 => {insert(sector); true}
      case 1 => true
      case _ => throw new Exception("Invalid number of updated rows")
    }
  }

  def insert(sector: BusinessSector) = {
    Logger.debug(s"Storing sector ${sector.id} for ${sector.venueId}")
    val sql =
      """
        |insert into business_sector(id, venue_id, row_scheme)
        |values({id}, {venueId}, {rowScheme})
      """.stripMargin
    Logger("sql").debug(sql)
    DB.withConnection { implicit connection =>
      SQL(sql).on(
        'id -> sector.id,
        'venueId -> sector.venueId,
        'rowScheme -> sector.rowScheme.toString
      ).executeInsert(scalar[String].singleOpt)
    }
  }

  def update(sector: BusinessSector) = {
    Logger.debug(s"updating sector ${sector.id} for ${sector.venueId}")
    val sql =
      """
        |update business_sector set row_scheme = {rowScheme}
        |where id = {id} and venue_id = {venueId}
      """.stripMargin
    Logger("sql").debug(sql)
    DB.withConnection { implicit connection =>
      SQL(sql).on(
        'id -> sector.id,
        'venueId -> sector.venueId,
        'rowScheme -> sector.rowScheme.toString
      ).executeUpdate()
    }
  }

}

object BusinessSectorRow {

  // ~parser
  def simple = {
    getAliased[String]("ROW_SECTOR_ID") ~
    getAliased[Long]("ROW_VENUE_ID") ~
    getAliased[Int]("ROW_ROW") ~
    getAliased[Int]("ROW_SEATS") map {
      case sectorId ~ venueId ~ row ~ seats => BusinessSectorRow(sectorId, venueId, row, seats)
    }
  }

  // ~queries

  def findForVenueAndSector(venueId: Long, sector: String) = {
    Logger.debug(s"Loading rows for $sector in $venueId")
    val sql =
      """
        |select sector_id as row_sector_id, venue_id as row_venue_id, row as row_row, seats as row_seats
        |from business_sector_row as row
        |where venue_id = {venueId} and sector_id = {sectorId}
      """.stripMargin
    DB.withConnection { implicit connection =>
      SQL(sql).on(
        'venueId -> venueId,
        'sectorId -> sector
      ).as( simple *)
    }
  }

  // ~updates
  
  
  def insertOrUpdate(sectorRow: BusinessSectorRow) = {
    update(sectorRow) match {
      case 0 => { insert(sectorRow); true}
      case 1 => true
      case _ => throw new Exception("Ivalid number of updated rows")
    }
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
      ).executeUpdate
    }
  }

  def insert(row: BusinessSectorRow): Unit = {
    Logger.debug(s"Inserting row ${row.row} to sector ${row.sectorId} in venue ${row.venueId}")
    val sql =
      """
        |insert into business_sector_row(sector_id, venue_id, row, seats)
        |values({sectorId}, {venueId}, {row}, {seats})
      """.stripMargin
    Logger("sql").debug(sql)
    DB.withConnection { implicit connection =>
      SQL(sql).on(
	    'sectorId  -> row.sectorId,
        'venueId   -> row.venueId,
        'row       -> row.row,
        'seats     -> row.seats
      ).executeInsert(scalar[String].singleOpt)
    }
  }

}