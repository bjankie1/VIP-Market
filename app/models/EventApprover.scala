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

  // ~parsers

  def simple: RowParser[EventApprover] = {
    long("event_approver.event_id") ~
    long("event_approver.user_id") map {
      case eventId ~ userId => EventApprover(eventId, userId)
    }

  }

}