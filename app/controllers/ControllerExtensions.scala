package controllers

import play.mvc.Controller
import play.api.mvc.Request
import play.api.mvc.MultipartFormData
import play.api.libs.Files.TemporaryFile
import play.api.Logger
import java.util.UUID

trait ControllerExtensions {

  /**
   * Extension to multi-part form request.
   */
  implicit def asExtendedRequest( r: Request[MultipartFormData[TemporaryFile]]) = new ExtendedRequest(r) 
  	class ExtendedRequest(r: Request[MultipartFormData[TemporaryFile]]) {
	  def upload(owner: String): Seq[UUID] = {
        val ids = r.body.files.filter(!_.filename.isEmpty()) map { picture =>
          Logger.info(s"Saving ${picture.filename}")
          val filename = picture.filename
          val contentType = picture.contentType
          models.File.store(picture.filename, owner, picture.ref.file)
        }
	    ids
	  }
  }
}