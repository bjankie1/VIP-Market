package models

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import org.joda.time.DateTime
import anorm.Pk
import anorm.NotAssigned

class EventModelSpec extends Specification {

  "Event model" should {

    "be retrieved by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val Some(fakeEvent) = Event.findById(2)
        fakeEvent.name must equalTo("Siatka")
      }
    }

    "be retrieved by date range" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val listOfEvents = Event.findByStartDate(DateTime.parse("2012-12-10"), DateTime.parse("2012-12-30"))
        listOfEvents.size must equalTo(2)
      }
    }

    "be inserted" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val id = Event.insert(Event(NotAssigned, "test", "test", DateTime.parse("2012-12-10"), 1, 1, true, DateTime.now()))
        id must be greaterThan (0)
      }
    }
  }

  "be activated and disactivated" in {
    running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      Event.activate(1, false)
      Event.findById(1).get.active must beFalse
      Event.activate(1, true)
      Event.findById(1).get.active must beTrue
    }
  }

}