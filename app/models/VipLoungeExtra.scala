package models

import anorm.Pk

/**
 * Additional request related to given VIP lounge
 */
case class VipLoungeExtra(
    id: Pk[Long],
    name: String,
    price: BigDecimal)