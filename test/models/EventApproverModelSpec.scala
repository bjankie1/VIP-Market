package models

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._


/**
 * Tests for models.EventApprover
 * User: fox
 * Date: 2/25/13
 * Time: 11:08 PM
 * To change this template use File | Settings | File Templates.
 */
class EventApproverModelSpec extends Specification {

  "EventApprover model should" should {

    "update all approvers for event" in {
      running(FakeApplication( additionalConfiguration = inMemoryDatabase())) {
        EventApprover.replace(1l, List(1l,2l,3l))
        val approvers = EventApprover.approversForEvent(1l)
        approvers.size must beEqualTo(3)

        EventApprover.replace(1l, List(3l, 4l))
        val approvers2 = EventApprover.approversForEvent(1l)
        approvers2.size must beEqualTo(2)

      }
    }

  }

}
