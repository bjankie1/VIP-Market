package views.html.helper


/**
 * Created with IntelliJ IDEA.
 * User: fox
 * Date: 2/23/13
 * Time: 10:35 PM
 * To change this template use File | Settings | File Templates.
 */
package object inputDateTime {

  implicit val dateTimeField = new FieldConstructor {
    def apply(elts: FieldElements) = dateTimeFieldConstructor(elts)
  }
}
