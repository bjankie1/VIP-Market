package models

import anorm._

case class EventType(
    id: Pk[Long],
    name: String)