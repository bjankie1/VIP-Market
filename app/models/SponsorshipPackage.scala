package models

import anorm._

/**
 * Sponsorship of given Event.
 */
case class SponsorshipPackage( 
    id: Pk[Long],
    event: Event,
    description: String)