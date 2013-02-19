package models

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import org.joda.time.DateTime
import anorm.Pk
import anorm.NotAssigned

class AccountModelSpec extends Specification {

  "Account model" should {

    "generate hash from password" in {
      val hash = Account.encodePassword("test")
      System.out.println(hash)
      Account.verifyPassword(hash._1, "test", hash._2) must beEqualTo(true)
      Account.verifyPassword(hash._1, "wrongpass", hash._2) must beEqualTo(false)
      Account.verifyPassword(hash._1, "test", hash._2  + "garbage") must beEqualTo(false)
    }
  }
}