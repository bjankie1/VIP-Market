package models

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class AccountModelSpec extends Specification {

  "Account model" should {

    "generate hash from password" in {
      val hash = Account.createPasswordHash("test")
      System.out.println(hash)
      Account.verifyPassword(hash._1, "test", hash._2) must beEqualTo(true)
      Account.verifyPassword(hash._1, "wrongpass", hash._2) must beEqualTo(false)
      Account.verifyPassword(hash._1, "test", hash._2  + "garbage") must beEqualTo(false)
    }


    "create new user" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
	      val id = Account.create("test@gmail.com", "test", "test", NormalUser)
	      id must beGreaterThan(0l)
      }
    }
    
    "pick ids and names" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
	      val idNameList = Account.idNameOnly("%tester%")
	      idNameList.size must beGreaterThan(0)
	      idNameList.forall(idname => idname._2.contains("tester"))
      }
    }    
  
  }
}