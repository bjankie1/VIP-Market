package models

import org.joda.time.DateTime
import anorm._
import anorm.SqlParser._
import java.sql.Clob
import play.api.Logger
import play.api.db.DB
import play.api.Play.current
import controllers.VipLoungeController

/**
  * Representation of purchase flow. Purchase starts with booking request. The request
  * needs to be confirmed by Organization representative. Once booking is confirmed it
  * needs to be paid.
  *
  * @author Bartosz Jankiewicz
  */
case class BookingOrder(
  id: Pk[Long],
  customerId: Long,
  dateCreated: DateTime,
  confirmedAt: Option[DateTime],
  confirmedBy: Option[String],
  paidAt: Option[DateTime],
  paidBy: Option[String],
  orderStatus: OrderStatus.Type)

/**
 * Enumeration providing values for status of Order
 */
object OrderStatus extends Enumeration {
  type Type = Value
  val booked, purchased, cancelled, rejected = Value
}

  
object BookingOrder extends AbstractModel {
  
  def simple: RowParser[BookingOrder] = {
    get[Pk[Long]]("booking_order.id") ~
    long("booking_order.customer_id") ~
    get[DateTime]("booking_order.date_created")  ~
    get[Option[DateTime]]("booking_order.date_confirmed")  ~
    get[Option[String]]("booking_order.confirmed_by")  ~
    get[Option[DateTime]]("booking_order.date_paid")  ~
    get[Option[String]]("booking_order.paid_by")  ~
    str("booking_order.order_status") map {
      case id ~ customerId ~ dateCreated ~ confirmedAt ~ confirmedBy ~ paidAt ~ paidBy ~ orderStatus => 
        BookingOrder(
          id, 
          customerId, 
          dateCreated, 
          confirmedAt, 
          confirmedBy, 
          paidAt, 
          paidBy,
          OrderStatus.withName(orderStatus))
    }
  }
}

/**
 * Base subject of an order. It defines substance of the order.
 */
abstract class OrderSubject( id: Pk[Long], orderId: Long, price: BigDecimal, dateCreated: DateTime)

/**
 * Purchase of VIP Lounge
 */
case class VipLoungeOrder( id: Pk[Long], orderId: Long, price: BigDecimal, dateCreated: DateTime, vipLoungeAtEventId: Long)
extends OrderSubject( id, orderId, price, dateCreated)


object VipLoungeOrder extends AbstractModel {
  
  def simple: RowParser[VipLoungeOrder] = {
    get[Pk[Long]]("vip_lounge_order.id") ~
    long("vip_lounge_order.order_id") ~
    get[java.math.BigDecimal]("vip_lounge_order.price") ~
    get[DateTime]("vip_lounge_order.date_created") ~
    long("vip_lounge_order.vip_lounge_at_event") map {
      case id ~ orderId ~ price ~ dateCreated ~ vipLoungeAtEventId => 
        VipLoungeOrder(id, orderId, price, dateCreated, vipLoungeAtEventId)
    }
  }
  
  def findById(id: Long) = {
    val sql = "select * from vip_lounge_order where id = {id}"
    Logger("sql").debug(sql)
    DB.withConnection{ implicit connection =>
      SQL(sql).on('id -> id).as(simple singleOpt)
    }
  }
  
  /**
   * Find orders referring to given containing order
   */
  def findByOrderId(orderId: Long) = {
    val sql = "select * from vip_lounge_order where order_id = {id}"
    Logger("sql").debug(sql)
    DB.withConnection{ implicit connection =>
      SQL(sql).on('id -> orderId).as(simple *)
    }
  }
  
  
  
  def save(order: VipLoungeOrder): Long = {
    val sql = """
      |update vip_lounge_order(order_id, price, date_created, vip_lounge_at_event)
      |values({orderId}, {price}, {dateCreated}, {vipLoungeAtEventId})
      """.stripMargin
    Logger("sql").debug(sql)
    DB.withConnection{implicit connection => 
      SQL(sql).on(
        'orderId -> order.orderId,
        'price   -> order.price,
        'dateCreated -> order.dateCreated,
        'vipLoungeAtEvent -> order.vipLoungeAtEventId
      ).executeInsert() match {
        case None => throw new Exception("VIP lounge order record store failed")
        case Some(id) => id
      }
    }
  }
  
}


/**
  * Purchase of business seats.
  */
case class BusinessSeatOrder( id: Pk[Long], orderId: Long, price: BigDecimal, dateCreated: DateTime, eventId: Long) 
extends OrderSubject( id, orderId, price, dateCreated)

/**
  * Class describes purchased seat + Business club package
  */
case class BusinessSeat( seatOrderId: Long, sectorId: String, row: Int, seat: Int)

object BusinessSeatOrder extends AbstractModel {
  val TABLE_NAME = "business_seat_order"
    
  def simple: RowParser[BusinessSeatOrder] = {
    get[Pk[Long]](s"$TABLE_NAME.id") ~
    long(s"$TABLE_NAME.order_id") ~
    get[java.math.BigDecimal](s"$TABLE_NAME.price") ~
    get[DateTime](s"$TABLE_NAME.date_created") ~
    long(s"$TABLE_NAME.event_id") map {
      case id ~ orderId ~ price ~ dateCreated ~ eventId => BusinessSeatOrder(id, orderId, price, dateCreated, eventId)
    }
  }
  
  def save( order: BusinessSeatOrder): Long = {
    val sql = """
      |insert into business_seat_order(order_id, price, date_created, event_id)
      |values({orderId}, {price}, {dateCreated}, {eventId})
      """.stripMargin
    Logger("sql").debug(sql)
    DB.withConnection { implicit connection =>
      SQL(sql).on(
        'orderId -> order.orderId,
        'price   -> order.price,
        'dateCreated -> order.dateCreated,
        'eventId -> order.eventId
      ).executeInsert() match {
        case None => throw new Exception("Error saving Business Seats order")
        case Some(id) => id 
      }
    }
  }
  
  def findById(id: Long) = {
    Logger.debug(s"Looking for business seat order $id")
    val sql = "select * from business_seat_order where id = {id}"
    DB.withConnection { implicit connection =>
      SQL(sql).on(
        'id -> id
      ).as( simple singleOpt)
    }
  }
  
  def findByOrderId(orderId: Long) = {
    Logger.debug(s"Looking for business seat order for order $orderId")
    val sql = "select * from business_seat_order where order_id = {id}"
    DB.withConnection { implicit connection =>
      SQL(sql).on(
        'id -> orderId
      ).as( simple *)
    }
  }
  
}