package utils

/**
 * Useful collection algorithms.
 * User: fox
 * Date: 3/4/13
 * Time: 10:58 PM
 */
object Collections {

  /**
   * Transforms list of numbers to corresponding range string. Eg.
   * List(1,2,3,8,11,12) -> "1-3,8,11-12"
   * @param numbers Sequence of numbers. No need for it to be sorted
   * @return Returns Sequence describing ranges
   */
  def numbersToRanges(numbers: Seq[Int]): String = {

    def aggregate(res: List[(Int,Int)], tail: Seq[(Int,Int)]): Seq[(Int, Int)] = {
      tail match {
        case Nil => res
        case _   => {
          if ( tail.head._2 - tail.head._1 == 1) {
            //merge adjecent ranges
            val merged = res.dropRight(1) :+ (res.last._1, tail.head._2)
            aggregate(merged, tail.tail)
          } else {
            //add new range
            aggregate(res :+ (tail.head._2, tail.head._2), tail.tail)
          }
        }
      }
    }

    val slided = numbers.sorted.sliding(numbers.size - 1).toList
    val pairs = slided.head.zip(slided.last)
    val aggr = aggregate(List(pairs.head), pairs.tail)
    aggr.map{ case (l, r) => {
      if( l != r) {
        s"$l-$r"
      } else {
        l.toString
      }
    }
    }.reduce((a, b) => s"$a,$b")
  }

}
