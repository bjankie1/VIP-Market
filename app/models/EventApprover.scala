package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import play.api.Logger

/**
 * User authorized to approve bookings for given Event
 * User: fox
 * Date: 2/24/13
 * Time: 12:14 AM
 * To change this template use File | Settings | File Templates.
 */
case class EventApprover(
  userId: Long,
  eventId: Long
)

object EventApprover {

  val insertSql =
    """
      |insert into event_approver(event_id, user_id)
      |values({eventId}, {userId})
    """.stripMargin

  // ~parsers

  def simple: RowParser[EventApprover] = {
    long("event_approver.event_id") ~
    long("event_approver.user_id") map {
      case eventId ~ userId => EventApprover(userId, eventId)
    }
  }

  // ~queries

  def approversForEvent(eventId: Long) = {
    Logger.debug(s"Looking for approvers for event $eventId")
    val sql =
      """
        |select * from event_approver where event_id = {eventId}
      """.stripMargin
    Logger("sql").debug(sql)
    val result = DB.withConnection { implicit connection =>
      SQL(sql).on(
        'eventId -> eventId
      ).as(simple *)
    }

    Logger.debug(s"Found ${result.size} approvers for event $eventId")
    result
  }

  // ~updates

  /**
   * Add new approver to the database. The operation assumes, that there is no approver
   * record yet. If the assumption is wrong the database may reject the insert operation
   * due to constraint violation.
   * @param approver Approver object
   */
  def save(approver: EventApprover) {
    Logger.debug(s"Saving approver ${approver.userId} for event ${approver.eventId}")
    Logger("sql").debug(insertSql)
    DB.withConnection { implicit connection =>
      SQL(insertSql).on(
        'eventId -> approver.eventId,
        'userId  -> approver.userId
      ).executeInsert()
    }
  }



  /**
   * Add all approvers related to given event. The operation replaces current approvers with
   * the new set
   * @param eventId Event to replace approvers for
   * @param approvers List of ids of approving users
   */
  def replace(eventId: Long, approvers: Seq[Long]) {
    Logger.debug(s"Saving $approvers approvers for event $eventId")
    val deleteSql =
      """
        |delete from event_approver
        |where event_id = {eventId}
      """.stripMargin
    DB.withTransaction { implicit connection =>
      SQL(deleteSql).on(
        'eventId -> eventId
      ).executeUpdate()
      approvers.map { userId =>
        SQL(insertSql).on(
          'eventId -> eventId,
          'userId  -> userId
        ).executeInsert()
      }
    }
  }

}