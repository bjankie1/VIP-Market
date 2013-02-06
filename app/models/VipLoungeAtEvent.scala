package models

import anorm.Pk

case class VipLoungeAtEvent(
    id: Pk[Long],
    eventId: Long,
    loungeId: Long,
    basePrice: BigDecimal
)