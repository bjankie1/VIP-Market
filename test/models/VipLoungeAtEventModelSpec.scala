package models

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import org.joda.time.DateTime
import anorm.Pk
import anorm.NotAssigned

class VipLoungeAtEventModelSpec extends Specification {

  "VipLoungeAtEvent model" should {
    
    "be inserted and then loaded" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        VipLoungeAtEvent.create(VipLoungeAtEvent(1, 666, 666.0, true))
        val Some(fakeLoungeAtEvent) = VipLoungeAtEvent.load(1, 666)
        fakeLoungeAtEvent.basePrice must equalTo(666.0)
      }
    }

    "be retrieved by event and VIP lounge ids" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        VipLoungeAtEvent.create(VipLoungeAtEvent(1, 1, 10.0, true))
        val Some(fakeLoungeAtEvent) = VipLoungeAtEvent.load(1, 1)
        fakeLoungeAtEvent.basePrice must equalTo(10.0)
      }
    }

    "be searched by event ID" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val listOfVipLounges = VipLounge.findByVenue(1)
        listOfVipLounges.size must equalTo(2)
        listOfVipLounges.forall(_.venueId equals 1l) must beTrue
      }
    }

    "be updated" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        //given
        VipLoungeAtEvent.create(VipLoungeAtEvent(1, 666, 666, true))
        VipLoungeAtEvent.update(VipLoungeAtEvent(1, 666, 6660.0, true))
        val Some(fakeLoungeAtEvent) = VipLoungeAtEvent.load(1, 666)
        fakeLoungeAtEvent.basePrice must equalTo(6660.0)
      }
    }
  }
}