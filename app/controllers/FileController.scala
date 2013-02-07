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
import play.api.libs.Files.TemporaryFile

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
  
  def pictures = UploadPictures { implicit request =>
    Logger.info("Uploading pictures")
    Ok(s"uploaded ${request.files.size} files")
  }

  def add = Action { implicit request =>
    Ok(views.html.admin.uploadfile.apply)
  }

  def UploadPictures(f: FileRequest => Result) = {
    Action(parse.multipartFormData) { request =>
      val ids = request.body.files.filter(!_.filename.isEmpty()) map { picture =>
        Logger.info(s"Saving ${picture.filename}")
        val filename = picture.filename
        val contentType = picture.contentType
        File.store(picture.filename, "unknown", picture.ref.file)
      }
      f(FileRequest(ids, request))
    }
  }
  
}

  case class FileRequest(
    files: Seq[UUID],
    private val request: Request[MultipartFormData[TemporaryFile]]) extends WrappedRequest(request)

