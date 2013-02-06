package models

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import org.joda.time.DateTime
import anorm.Pk
import anorm.NotAssigned

class VenueModelSpec extends Specification {

  "Venue model" should {

    "be retrieved by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val Some(fakeVenue) = Venue.findById(2)
        fakeVenue.name must equalTo("Stadion narodowy")
      }
    }

    "be searched by city" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val listOfVenues = Venue.findByCity("Wrocław")
        listOfVenues.size must equalTo(1)
        listOfVenues.forall(_.address.city equals "Wrocław") must beTrue
      }
    }

    "be searched by name" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val listOfVenues = Venue.findByName("Stadion miejski")
        listOfVenues.size must equalTo(1)
        listOfVenues.forall(_.name equals "Stadion miejski") must beTrue
      }
    }

    "all be loaded" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val listOfVenues = Venue.getAll
        listOfVenues.size must equalTo(3)
      }
    }

    "be inserted" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val venue = Venue(
            NotAssigned, 
            "test", 
            "test", 
            Address("Testowa", "Miasto", Country("Państwo")), 
            true, 
            DateTime.parse("2012-12-10"))
        val id = Venue.insert(venue)
        id must be greaterThan(0)
      }
    }
    

    "be updated" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        //given
        val venueOriginal = Venue(
            NotAssigned, 
            "test", 
            "test", 
            Address("Testowa", "Miasto", Country("Państwo")), 
            true, 
            DateTime.parse("2012-12-10"))
        //when
        Venue.update(1, venueOriginal)
        //then
        val venueUpdated = Venue.findById(1)
        venueUpdated must beSome[Venue].which(_.name.equals("test"))
      }
    }    
  }
}