package controllers

import models.{BusinessSectorRow, DisplayScheme, BusinessSector, BusinessSeat}
import play.api.data.Forms._
import play.api._
import play.api.mvc._
import play.api.data._
import utils.Collections._



/**
 * Actions related to managing business seats for venues.
 * User: fox
 * Date: 3/3/13
 * Time: 9:04 PM
 */
object BusinessSeatController extends BaseController {

  /**
   * Representation of a single sector
   * @param rowScheme Row naming scheme
   * @param seatScheme Seats naming scheme
   * @param rowsSeats List of ranges describing seats distribution
   */
  case class Sector(rowScheme: String, seatScheme: String, rowsSeats: List[RowsRangeSeats]) {

    /**
     * Convert this view object to models object
     */
    def toBusinessSector(id: String, venueId: Long) = 
      BusinessSector(id, venueId, DisplayScheme.withName(rowScheme))
      
    def sectorRows(id: String, venueId: Long): Seq[BusinessSectorRow] = {
      rowsSeats.map( row => 
        parseRange(row.rowsRanges.split(",")).map(
          BusinessSectorRow(id, venueId, _, row.seats))
        ).flatten
    }
  }

  /**
   * Representation of seats distribution in given rows. It uses range expression that uses hyphen to represent continuous range
   * eg. 1-10 and coma to separate ranges. The valid range is 1-10,12 or 1,3,5,6-10,15,20-100
   * @param rowsRanges Rows range expression.
   * @param seats Number of seats for specified rows identified with range expression
   */
  case class RowsRangeSeats(rowsRanges: String, seats: Int)

  object RowsRangeSeats {

    def isValid(s: String): Boolean = {

      /**
       * Verifies if set of numbers is monotonous. Meaning: nums[i] < nums[i+1]
       */
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
        case Nil       => {
          Logger.warn(s"Rows range $s is not a valid expression")
          false
        }
        case numbers   => {
          Logger.info(s"Rows range $s is a valid expression")
          true
        }
      }
    }


    def fromRows( rows: Seq[BusinessSectorRow]): List[RowsRangeSeats] = rows match {
      case Nil => Nil
      case _ => rows.groupBy(_.seats).map {
          case (seats,rws) => RowsRangeSeats(numbersToRanges(rws.map(_.row)), seats)
        }.toList.sortBy(_.rowsRanges.takeWhile(c => c != ',' && c != '-').toInt)
    }
  }

  val form: Form[Sector] = Form (
    mapping(
      "rowScheme" -> text().verifying("business.seats.invalid.scheme", value => DisplayScheme.values.exists(_.toString.equalsIgnoreCase(value))),
      "seatsScheme" -> text().verifying("business.seats.invalid.scheme", value => DisplayScheme.values.exists(_.toString.equalsIgnoreCase(value))),
      "rowsSeats" -> Forms.list(mapping(
        "rowsRanges" -> nonEmptyText.verifying("business.class.seats.range.invalid", RowsRangeSeats.isValid(_)),
        "seats"     -> number(min = 1, max = 1000)
      )(RowsRangeSeats.apply)(RowsRangeSeats.unapply))
    )(Sector.apply)(Sector.unapply)
  )
  
  def sectors(venueId: Long) = authorizedAction(authorizeAdmin) { user => implicit request =>
    Logger.info(s"Displaying sectors for $venueId")
    Ok(views.html.admin.seats.list(venueId, BusinessSector.findForVenue(venueId)))
  }

  def edit(venueId: Long, sectorId: String) = authorizedAction(authorizeAdmin) {
    user => implicit request =>
      Logger.info(s"Editing sector $sectorId for venue $venueId")
      val businessSector = BusinessSector.findSector(venueId, sectorId)
      val rows = BusinessSectorRow.findForVenueAndSector(venueId, sectorId)
      val viewSector: Sector = businessSector match {
        case None => Sector(DisplayScheme.Numeric.toString, DisplayScheme.Numeric.toString, Nil)
        case Some(bs) => {
          val sector = Sector(
            bs.rowScheme.toString, DisplayScheme.Numeric.toString, RowsRangeSeats.fromRows(rows)
          )
          sector
        }
      }
      Ok(views.html.admin.seats.form(venueId, sectorId, form.fill(viewSector)))
  }

  def update(venueId: Long, sectorId: String) = authorizedAction(authorizeAdmin) {
    user => implicit request =>
      form.bindFromRequest.fold(
          errors => {
            Logger.warn("Invalid form values")
            BadRequest(views.html.admin.seats.form(venueId, sectorId, errors)) 
          },
          sector => {
            Logger.info("Saving available rows information")
            BusinessSector.insertOrUpdate(sector.toBusinessSector(sectorId, venueId))
            sector.sectorRows(sectorId, venueId).map( row =>
              BusinessSectorRow.insertOrUpdate(row)
            )
            Redirect(routes.VenueController.list)
          })
  }
  
  
  def javascriptRoutes = Action { implicit request =>

    import play.api.Routes

    // apiRoutes is the name of the router in
    // the global javascript scope
    Ok(Routes.javascriptRouter("apiRoutes")(
      routes.javascript.BusinessSeatController.edit
    )).as(JAVASCRIPT)
  }
  

}
