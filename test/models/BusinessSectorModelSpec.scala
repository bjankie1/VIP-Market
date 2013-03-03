package models

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

/**
 * Test for BusinessSectorRow
 * User: fox
 * Date: 3/3/13
 * Time: 12:18 AM
 */
class BusinessSectorModelSpec extends Specification {

  "BusinessSectorModel" should {

    "findForVenue" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val sectors = BusinessSector.findForVenue(1l)
        sectors.size must beGreaterThan(0)
      }
    }

    "saveNewSector" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        BusinessSector.insert(BusinessSector("C", 1, DisplayScheme.Roman))
        val sectors = BusinessSector.findForVenue(1)
        sectors.exists(_.id.equals("C")) must beTrue
      }
    }

    "updateSector" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        BusinessSector.update(BusinessSector("A", 1, DisplayScheme.Letter))
        val sectors = BusinessSector.findForVenue(1)
        sectors.filter(_.id.equals("A")).head.rowScheme must beEqualTo(DisplayScheme.Letter)
      }
    }

  }

  "BusinessSectorRowModel" should {

    "findForVenueAndSector" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val rows = BusinessSectorRow.findForVenueAndSector(1, "A")
        rows.size must beEqualTo(2)
      }
    }


    "saveNewSectorRow" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        BusinessSectorRow.insert(BusinessSectorRow("C", 1, 1, 1))
        val sectorRows = BusinessSectorRow.findForVenueAndSector(1, "C")
        sectorRows.exists(_.sectorId.equals("C")) must beTrue
      }
    }

    "updateSectorRow" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        BusinessSectorRow.update(BusinessSectorRow("A", 1, 1, 666))
        val rows = BusinessSectorRow.findForVenueAndSector(1, "A")
        rows.filter(_.row == 1).forall(_.seats == 666) must beTrue
      }
    }


  }
}
