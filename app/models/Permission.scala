package models

sealed trait Permission {
  
  def isAdmin = this match {
    case Administrator => true
    case _ => false
  }

  def isNormalUserOrAdmin = true
}

case object Administrator extends Permission

case object NormalUser extends Permission