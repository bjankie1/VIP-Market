package models

sealed trait Permission {
  
  def isAdmin = this match {
    case Administrator => true
    case _ => false
  }

  def isNormalUserOrAdmin = true
}

object Permission {
  
  /**
   * Returns a Permission object or None if value does not match any permission
   */
  def fromString( value: String): Option[Permission] = {
    value match {
        case "Administrator" => Some(Administrator)
        case "NormalUser"    => Some(NormalUser)
        case _               => None
      }
  }
  
}

case object Administrator extends Permission {
  override def toString = "Administrator"
}

case object NormalUser extends Permission {
  override def toString = "NormalUser"
}