package models

import anorm.Pk

/**
 * Information about commercial minutes available for given event.
 */
case class CommercialsMinutes(
    id: Pk[Long],
    eventId: Long,
    minutes: Int,
    pricePerMinute: BigDecimal
)
