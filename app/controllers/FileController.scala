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

object FileController extends BaseController {

  def editFiles(ownerId: String) = Action { implicit request =>
    val files = File.findByOwner(ownerId)
    Ok(views.html.admin.tags.filelist(files))
  }

  def scaledImage(id: String) = Action {
    val stream = File.resize(UUID.fromString(id), 100, 100)
    SimpleResult(
      header = ResponseHeader(200),
      body = Enumerator.fromStream(stream)
    )
  }


  def list = Action { implicit request =>
    Ok(views.html.admin.files( File.list))
  }
  
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
  
  def pictures = UploadPictures("general") { implicit request =>
    Logger.info("Uploading pictures")
    Ok(s"uploaded ${request.files.size} files")
  }

  def add = Action { implicit request =>
    Ok(views.html.admin.uploadfile.apply)
  }
  
}

  case class FileRequest(
    files: Seq[UUID],
    private val request: Request[MultipartFormData[TemporaryFile]]) extends WrappedRequest(request)

