package models

import org.joda.time.DateTime

/**
 * Information about a user whos is entitled to approve bookings.
 */
case class OrganizationApprover(
    userId: String,
    organizationId: Long,
    addedBy: String,
    createdAt: DateTime
)