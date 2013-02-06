package models

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import org.joda.time.DateTime
import anorm.Pk
import anorm.NotAssigned
import play.api.Logger

class FileModelSpec extends Specification {

  "File model" should {

    "save file to storage" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val file = java.io.File.createTempFile("test", "file")
        val id = File.store("test file", "tester", file)
        Logger.info(id.toString())
        id must be_!=(false)
      }
    }
  }
  
}