package controllers

import models.{BusinessSectorRow, DisplayScheme, BusinessSector, BusinessSeat}
import play.api.data.Form
import play.api.data.Forms._
import play.api._
import i18n.Messages
import play.api.data._
import play.api.mvc._
import utils.Collections._



/**
 * Actions related to managing business seats for venues.
 * User: fox
 * Date: 3/3/13
 * Time: 9:04 PM
 */
class BusinessSeatController extends BaseController {

  /**
   * Representation of a single sector
   * @param id Identifier
   * @param venueId Id of Venue
   * @param rowScheme Row naming scheme
   * @param seatScheme Seats naming scheme
   * @param rowsSeats List of ranges describing seats distribution
   */
  case class Sector(id: String, venueId: Int, rowScheme: String, seatScheme: String, rowsSeats: List[RowsRangeSeats])

  /**
   * Representation of seats distribution in given rows. It uses range expression that uses hyphen to represent continuous range
   * eg. 1-10 and coma to separate ranges. The valid range is 1-10,12 or 1,3,5,6-10,15,20-100
   * @param rowsRanges Rows range expression.
   * @param seats Number of seats for specified rows identified with range expression
   */
  case class RowsRangeSeats(rowsRanges: String, seats: Int)

  object RowsRangeSeats {

    def isValid(s: String): Boolean = {

      def monotonous(nums: Seq[Int]): Boolean = {
        nums match {
          case Nil => true
          case _   => {
            nums.size match {
              case 1 => true
              case _ => nums.head < nums.tail.head && monotonous(nums.tail)
            }
          }
        }
      }

      parseRange(s.split(",")) match {
        case Nil       => false
        case numbers   => {

          true
        }
      }
    }


    def fromRows( rows: Seq[BusinessSectorRow]): List[RowsRangeSeats] = rows match {
      case Nil => Nil
      case someRows => someRows.groupBy(_.seats).map {
          case (seats,rws) => RowsRangeSeats(numbersToRanges(rws.map(_.row)), seats)
        }.toList
    }

    /**
     * Parse range expression into list of integers
     * @param expressions Expressions sequence. Each expression is either a number or range N-M where M > N
     */
    def parseRange( expressions: Seq[String]): Seq[Int] = {
      expressions match {
        case Nil => Nil
        case _   => {
          val expr = expressions.head
          if (expr.matches("""\d+-\d+""")) {
            val pair = expr.split("-")
            val range = (pair.head.toInt to pair.last.toInt)
            range ++ parseRange(expressions.tail)
          } else {
            expr.toInt +: parseRange(expressions.tail)
          }
        }
      }
    }

  val form: Form[Sector] = Form (
    mapping(
      "id" -> nonEmptyText(1, 20),
      "venueId" -> number,
      "rowScheme" -> text().verifying(value => DisplayScheme.values.exists(_.toString.equalsIgnoreCase(value))),
      "seatsScheme" -> text().verifying(value => DisplayScheme.values.exists(_.toString.equalsIgnoreCase(value))),
      "rowsSeats" -> Forms.list(mapping(
        "rowsRanges" -> nonEmptyText.verifying("business.class.seats.range.invalid", RowsRangeSeats.isValid(_)),
        "seats"     -> number(min = 1, max = 1000)
      )(RowsRangeSeats.apply)(RowsRangeSeats.unapply))
    )(Sector.apply)(Sector.unapply)
  )

  def edit(venueId: Int, sectorId: String) = authorizedAction(authorizeAdmin) {
    user => implicit request =>
      Logger.info(s"Editing sector $sectorId for venue $venueId")
      val businessSector = BusinessSector.findSector(venueId, sectorId)
      val rows = BusinessSectorRow.findForVenueAndSector(venueId, sectorId)
      val viewSector = businessSector match {
        case None => Sector(sectorId, venueId, DisplayScheme.Numeric.toString, DisplayScheme.Numeric.toString, Nil)
        case Some(bs) => {
          val sector = Sector(
            sectorId, venueId, bs.rowScheme.toString, DisplayScheme.Numeric.toString, RowsRangeSeats.fromRows(rows)
          )
        }
      }
      Ok(views.html.admin.sectors.form(viewSector))
  }

}
