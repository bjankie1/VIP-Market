package controllers

/**
 * Performs actions related to seats or lounge booking
 */
object BookingController extends BaseController {

  /**
   * Order lounge at given Event
   */
  def orderLounge(eventId: Long, loungeId: Long) = TODO

  /**
   * Order seats in business class
   */
  def orderSeats(eventId: Long, sectorId: String) = TODO
    
  /**
   * Confirm the order
   */
  def confirmOrder(orderId: Long) = TODO
    
  /**
   * Confirm the order
   */
  def cancelOrder(orderId: Long) = TODO
  
}