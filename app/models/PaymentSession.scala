package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import java.math.BigDecimal

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

/**
  * Payment session for a VIP lounge or Business seats.
  */
case class PaymentSession(
  id: Pk[Long],
  bookingOrderId: Long,
  value: BigDecimal,
  dateStarted: DateTime,
  dateFinished: Option[DateTime],
  status: PaymentStatus.Type)

/**
 * Statuses of payment session
 */
object PaymentStatus extends Enumeration {
  type Type = Value
  val pending, accepted, rejected, cancelled = Value
}

object PaymentSession extends AbstractModel {

  def createNew(value: BigDecimal, bookingOrderId: Long) = 
    PaymentSession(NotAssigned, bookingOrderId, value, DateTime.now(), None, PaymentStatus.pending)
  
  implicit def rowToPaymentStatus: Column[PaymentStatus.Type] = Column.nonNull{(value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case status: String => Right(PaymentStatus.withName(status))
      case _ => Left(TypeDoesNotMatch("Cannot convert " + value + ":" + value.asInstanceOf[AnyRef].getClass))
    }
  }
  
  
  /**
    * Parse a Task from a ResultSet
    */
  val simple = {
    get[Pk[Long]]("payment_session.id") ~
    get[Long]("payment_session.booking_order_id") ~
    get[BigDecimal]("payment_session.value") ~
    get[DateTime]("payment_session.date_started") ~
    get[Option[DateTime]]("payment_session.date_finished") ~
    get[PaymentStatus.Type]("payment_session.status") map {
      case id ~ bookingOrderId ~ value ~ dateStarted ~ dateFinished ~ status => PaymentSession(
        id, bookingOrderId, value, dateStarted, dateFinished, status)
    }
  }

  // -- Queries

  /**
    * Retrieve a PaymentSession from the id.
    */
  def findById(id: Long): Option[PaymentSession] = {
    DB.withConnection { implicit connection =>
      SQL("select * from payment_session where id = {id}").on(
        'id -> id).as(PaymentSession.simple.singleOpt)
    }
  }

}