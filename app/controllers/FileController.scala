package controllers

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc._
import models.File
import java.util.UUID
import play.api.libs.iteratee.Enumerator
import views.html.defaultpages.notFound
import play.api.mvc.ChunkedResult
import play.api.mvc.ResponseHeader
import play.api.libs.iteratee.Iteratee

object FileController extends Controller {

  def load(id: String) = Action {
    Logger.debug(s"Loading file ${id}")
    val file = File.load(UUID.fromString(id))
    val result = file match {
      case Some(existing) => {
        val dataContent = Enumerator.fromStream(existing.asStream)
        Ok.stream(dataContent).withHeaders("Content-Disposition" -> s"attachment; filename=${existing.originalName}")
      }
      case None => NotFound
    }
    result
  }

  def upload = Action(parse.multipartFormData) { request =>
    Logger.info("Uploading file")
    request.body.file("picture").map { picture =>
      val filename = picture.filename
      val contentType = picture.contentType
      File.store(picture.filename, "unknown", picture.ref.file)
      Ok("File uploaded")
    }.getOrElse {
      Redirect(routes.Application.index).flashing(
        "error" -> "Missing file")
    }
  }
  
  def add = Action { implicit request =>
    Ok(views.html.admin.uploadfile.apply)
  }

}