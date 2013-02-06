package models

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import org.joda.time.DateTime
import anorm.Pk
import anorm.NotAssigned

class VipLoungeModelSpec extends Specification {

  "VipLounge model" should {

    "be retrieved by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val Some(fakeLounge) = VipLounge.findById(1)
        fakeLounge.name must equalTo("Loża szyderców")
      }
    }

    "be searched by venue" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val listOfVipLounges = VipLounge.findByVenue(666)
        listOfVipLounges.size must equalTo(1)
        listOfVipLounges.forall(_.venueId equals 666l) must beTrue
      }
    }

    "be inserted" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val lounge = VipLounge.createVipLounge(3)
        val id = VipLounge.insert(lounge)
        id must be greaterThan(0)
      }
    }
    
    "be activated and disactivated" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        VipLounge.activate(1, false)
        VipLounge.findById(1).get.active must beFalse
        VipLounge.activate(1, true)
        VipLounge.findById(1).get.active must beTrue
      }
    }
    

    "be updated" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        //given
        val loungeOriginal = VipLounge(
            NotAssigned, 
            "name",
            1,
            "lounge1",
            "A good lounge",
            1,
            true,
            DateTime.now())
        //when
        VipLounge.update(1, loungeOriginal)
        //then
        val loungeUpdated = VipLounge.findById(1)
        loungeUpdated must beSome[VipLounge].which(_.name.equals("name"))
      }
    }    
  }
}