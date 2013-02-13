package controllers

import anorm.Id
import anorm.Pk
import play.api.data.FormError
import play.api.data.FieldMapping
import play.api.data.Mapping
import play.api.data.Forms
import play.api.data.format.Formatter
import play.api.data.format.Formats
import play.api.mvc.Controller
import play.api.mvc.Result
import play.api.mvc.Action
import play.api.Logger

trait BaseController extends Controller with ControllerExtensions {
  
  /**
    * Formatter for the `Pk` type.
    *
    * @param pattern a date pattern, as specified in `java.text.SimpleDateFormat`.
    */
  def pkFormat: Formatter[Pk[Long]] = new Formatter[Pk[Long]] {

    override val format = Some(("format.long", Nil))

    def bind(key: String, data: Map[String, String]) = {
      parsing(s => Id(java.lang.Long.parseLong(s)), "error.number", Nil)(key, data)
    }

    def unbind(key: String, value: Pk[Long]) = Map(key -> value.toString)

    private def parsing[T](parse: String => T, errMsg: String, errArgs: Seq[Any])(key: String, data: Map[String, String]): Either[Seq[FormError], T] = {
      Formats.stringFormat.bind(key, data).right.flatMap { s =>
        scala.util.control.Exception.allCatch[T]
          .either(parse(s))
          .left.map(e => Seq(FormError(key, errMsg, errArgs)))
      }
    }
  }
  
  def pkLong: Mapping[Pk[Long]] = Forms.of[Pk[Long]](pkFormat) as pkFormat
  
  
  def UploadPictures(owner: String)(f: FileRequest => Result) = {
    Action(parse.multipartFormData) { request =>
      val ids = request.upload(owner)
      f(FileRequest(ids, request))
    }
  }

}