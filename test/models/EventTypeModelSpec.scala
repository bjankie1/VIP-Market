package models

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import org.joda.time.DateTime
import anorm.{Id, NotAssigned}

class EventTypeModelSpec extends Specification {

  "EventType model" should {


    "be retrieved by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val Some(fakeEventType) = EventType.findById(2)
        fakeEventType.name must equalTo("Siatkówka")
      }
    }

    "be retrieved as a list" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val fakeEventTypes = EventType.findAll
        fakeEventTypes.size must equalTo(3)
      }
    }

    "be inserted" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val id = EventType.insert(EventType(NotAssigned, "test"))
        id must be greaterThan (0)
      }
    }
  }

  "be updated" in {
    running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      val id: Long = EventType.update(1, EventType(Id(1), "Siaktówka plażowa"))
      id must beGreaterThan(0l)
      val Some(eventType) = EventType.findById(id)
      eventType.name must beEqualTo("Siaktówka plażowa")
    }
  }

}