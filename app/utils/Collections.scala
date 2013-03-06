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

    def toPairs(): Seq[(Int,Int)] = {
      numbers.size match {
        case 1 => Seq((numbers.head, numbers.head))
        case 2 => Seq((numbers.head, numbers.last))
        case _ => {
          val slided = numbers.sorted.sliding(numbers.size - 1).toList
          slided.head.zip(slided.last)
        }
      }
    }

    numbers match {
      case Nil => ""
      case _ => {
        val pairs = toPairs()
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

  }


  /**
   * Parse range expression into list of integers
   * @param expressions Expressions sequence. Each expression is either a number or range N-M where M > N
   */
  def parseRange( expressions: Seq[String]): Seq[Int] = {
    expressions match {
      case Nil => Nil
      case _   => {
        val expr = expressions.head
        if (expr.matches("""\d+-\d+""")) {
          val pair = expr.split("-")
          val range = (pair.head.toInt to pair.last.toInt)
          range ++ parseRange(expressions.tail)
        } else {
          expr.toInt +: parseRange(expressions.tail)
        }
      }
    }
  }


}
