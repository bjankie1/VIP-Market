package models

import org.joda.time.DateTime
import anorm._
import anorm.SqlParser._
import java.sql.Clob

case class BookingOrder(
  id: Pk[Long],
  customerId: Long,
  subjectId: Long,
  dateCreated: DateTime,
  confirmedAt: Option[DateTime],
  confirmedBy: Option[String],
  paidAt: Option[DateTime],
  paidBy: Option[String],
  orderStatus: OrderStatus.Type)

object OrderStatus extends Enumeration {
  type Type = Value
  val booked, purchased, cancelled, rejected = Value
}

abstract class OrderSubject(
  id: Pk[Long],
  price: BigDecimal,
  dateCreated: DateTime,
  eventId: Long)

/**
  * Purchase of business seats.
  */
case class BusinessSeatOrder(
  id: Pk[Long],
  seats: List[BusinessSeat])

/**
  * Representation of purchase flow. Purchase starts with booking request. The request
  * needs to be confirmed by Organization representative. Once booking is confirmed it
  * needs to be paid.
  *
  * @author Bartosz Jankiewicz
  */
case class VipLoungeOrder(
  id: Pk[Long],
  vipLoungeAtEventId: Long)

/**
  * Class describes purchased seat + Business club package
  */
case class BusinessSeat(
  id: Pk[Long],
  row: Int,
  seat: Int)
  
object BookingOrder extends AbstractModel {

  object Clob {
    def unapply(clob: Clob): Option[String] = Some(clob.getSubString(1, clob.length.toInt))
  }

  implicit val rowToPermission: Column[OrderStatus.Type] = {
    Column.nonNull[OrderStatus.Type] { (value, meta) =>
      value match {
        case Clob("booked") => Right(OrderStatus.booked)
        case Clob("cancelled")    => Right(OrderStatus.cancelled)
        case _ => Left(TypeDoesNotMatch(
          "Cannot convert %s : %s to OrderStatus for column %s".format(value, value.getClass, meta.column)))
      }
    }
  }
  
  def simple: RowParser[BookingOrder] = {
    get[Pk[Long]]("booking_order.id") ~
    get[Long]("booking_order.customer_id") ~
    get[Long]("booking_order.subject_id")  ~
    get[DateTime]("booking_order.subject_id")  ~
    get[Option[DateTime]]("booking_order.subject_id")  ~
    get[Option[String]]("booking_order.subject_id")  ~
    get[Option[DateTime]]("booking_order.subject_id")  ~
    get[Option[String]]("booking_order.subject_id")  ~
    get[OrderStatus.Type]("booking_order.subject_id") map {
      case id ~ customerId ~ subjectId ~ dateCreated ~ confirmedAt ~ confirmedBy ~ paidAt ~ paidBy ~ orderStatus => 
        BookingOrder(
          id, 
          customerId, 
          subjectId, 
          dateCreated, 
          confirmedAt, 
          confirmedBy, 
          paidAt, 
          paidBy,
          orderStatus)
    }
  }
}
