package utils

import org.specs2.mutable._

/**
 * User: fox
 * Date: 3/4/13
 * Time: 11:01 PM
 */
class CollectionsSpec extends Specification {

  "Collections" should {

    "numbersToRanges must collate numbers to ranges" in {
      Collections.numbersToRanges(1 to 5) must beEqualTo("1-5")
      Collections.numbersToRanges((1 to 5) ++ (7 to 9)) must beEqualTo("1-5,7-9")
      Collections.numbersToRanges((1 to 5) :+ 7) must beEqualTo("1-5,7")
      Collections.numbersToRanges(5 +: (7 to 9)) must beEqualTo("5,7-9")
      Collections.numbersToRanges(((1 to 5) :+ 7) ++ (9 to 11)) must beEqualTo("1-5,7,9-11")
    }

  }
}
