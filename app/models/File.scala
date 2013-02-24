package models

import java.util.UUID
import java.net.MalformedURLException
import java.net.URL
import play.api.Logger
import play.api.Application
import play.api.Play
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import java.io.{ByteArrayOutputStream, InputStream, ByteArrayInputStream, FileInputStream}
import scala.util.Random
import java.awt.image.BufferedImage
import com.mortennobel.imagescaling.ResampleOp
import javax.imageio.ImageIO

/**
 * Abstract file representation
 */
trait File {
  
  def id: UUID
  
  def name: String
  
  def size: Long
  
  def storage: String
  
  /**
   * Original name of stored file used to detect MIME type
   */
  def originalName: String
  
  /**
   * Save file to storage
   */
  def save( file: java.io.File): UUID
  
  /**
   * Delete file from storage
   */
  def delete
  
  /**
   * a tag pointing to file owner - where the file was initially uploaded eg. a Venue
   */
  def owner: String
  
  /**
   * Create inputstrem for reading file contents
   */
  def asStream: InputStream
}

case class S3File(id: UUID, name: String, size: Long, originalName: String, owner: String, bucket: String) extends File {

  def actualFileName = {
    id + "/" + name
  }

  def url =
    new URL("https://s3.amazonaws.com/" + bucket + "/" + actualFileName)

  def save( file: java.io.File) = {
    Logger.info(s"Saving file ${name} to S3 storage")
    id
  }
  
  def delete = {
    Logger.info(s"Deleting file ${name} from S3 storage")
  }
  
  def storage = "s3"
    
  def asStream = new ByteArrayInputStream("".getBytes())
}

case class LocalFile(id: UUID, name: String, size: Long, originalName: String, owner: String, storeFolder: String) extends File {
  
  /**
   * Implementation of save operation. Moves file to storeFolder
   */
  def save( file: java.io.File) = {
    Logger.info(s"Storing file ${id} - ${name} to local folder ${storeFolder}")
    val target = toFile(id)
    file.renameTo(target)
    Logger.info(s"File ${id} saved to ${target}")
    id
  }
  
  /**
   * Removes file from storeFolder
   */
  def delete = {
    Logger.info(s"Deleting file $id - $name from storage")
    toFile(id).delete()
  }
  
  def storage = "local"
    
  def asStream = {
    val parent = new java.io.File(storeFolder, id.toString().substring(0, 3))
    val target = new java.io.File(parent, id.toString)
    new FileInputStream(target)
  }

  def toFile( id: UUID) = {
    //parent folder based on the first 3 characters of id
    val parent = new java.io.File(storeFolder, id.toString().substring(0, 3))
    if( !parent.exists()) {
      if( parent.mkdirs()) {
        Logger.info(s"created parent folder ${parent}")
      } else {
        throw new IllegalStateException(s"Failed to create storage folder ${parent}")
      }
    }
    new java.io.File( parent, id.toString)
  }
}

object File {
  
  val fs = Play.current.configuration.getString("file.storage")
  Logger.info(fs.toString)
  val storageType = Play.current.configuration.getString("file.storage", Some(Set("local","s3"))) match {
    case Some("local") => "local"
    case Some("s3")    => "s3"
    case default       => throw new IllegalArgumentException("Invalid file.storage configuration")
  }
  
  lazy val storagePath = Play.current.configuration.getString("disk.path") match {
    case Some(path) => path
    case None       => throw new IllegalArgumentException("Missing file.storage.disk.path configuration")
  }
  
  lazy val s3bucket = Play.current.configuration.getString("aws.s3.bucket") match {
    case Some(existingBucket) => existingBucket
    case None => throw new IllegalArgumentException("Missing aws.s3.bucket configuration")
  }

    
  // -- Parsers

  /**
    * Parse a User from a ResultSet
    */
  val simple: RowParser[File] = {
    str("id") ~
    str("name") ~
    long("size") ~
    str("original_name") ~
    str("owner") ~
    str("storage") map {
       case id ~ name ~ size ~ fileName ~ owner ~ storage => storage match {
         case "local" => LocalFile(UUID.fromString(id), name, size, fileName, owner, storagePath)
         case "s3"    => S3File(UUID.fromString(id), name, size, fileName, owner, s3bucket)
       } 
    }
  }
  
  def list = {
    Logger.info("Listing all files")
    val sql = """
      select * from file_info order by name
      """
    Logger("sql").debug(sql)
    DB.withConnection { implicit connection =>
      SQL(sql).as(simple *)
    }
  }

  def findByOwner(ownerId: String) = {
    Logger.info(s"Listing all files for $ownerId")
    val sql =
      """
        |select * from file_info
        |where owner = {owner}
        |order by name
      """.stripMargin
    Logger("sql").debug(sql)
    DB.withConnection { implicit connection =>
      SQL(sql).on(
        'owner -> ownerId
      ).as(simple *)
    }
  }
  // -- DB store
  
  def insert(file: File, originalFile: java.io.File) {
    Logger.info(s"Storing file ${file.name} owned by ${file.owner}")
    val sql = """
      |insert into file_info(id, name, size, storage, original_name, owner)
      |values({id},{name},{size},{storage},{fileName}, {owner})
    """.stripMargin
    Logger("sql").debug(sql)
    DB.withConnection { implicit connection =>
      SQL(sql).on(
          'id        -> file.id,
          'name      -> file.name,
          'size      -> file.size,
          'storage   -> file.storage,
          'fileName  -> originalFile.getName(),
          'owner     -> file.owner
      ).executeInsert()
    }
    Logger.info(s"Stored file ${file.id}")
  }

  def store(name: String, owner: String, file: java.io.File): UUID = {
    val id = UUID.randomUUID
    val fileObject = storageType match {
      case "local" => LocalFile(id, name, file.length(), file.getName(), owner, storagePath)
      case "s3"    => S3File(id, name, file.length(), file.getName(), owner, s3bucket)
    }
    fileObject.save(file)
    insert(fileObject, file)
    fileObject.id
  }
  
  def load(id: UUID): Option[File] = {
    Logger.info(s"Loading file ${id}")
    val sql = """
      select * from file_info where id = {id}
      """
    Logger("sql").debug(sql)
    DB.withConnection{ implicit connection =>
      SQL(sql).on(
        'id -> id.toString()
      ).as(File.simple.singleOpt)
    }
  }
  
  def asStream(id: UUID): Option[(Long, InputStream)] = {
    val fileInfo = File.load(id)
    fileInfo match {
      case Some(file)  => Some((file.size, file.asStream))
      case None        => None
    }
  }

  def resize( id: UUID, width: Int, height: Int): InputStream = {
    val idStream = asStream(id)
    idStream match {
      case Some((size, stream)) => resize(stream, width, height)
      case None => throw new Exception("Not found")
    }
  }

  def resize(is: InputStream, width: Int, height: Int): InputStream = {
    val op = new ResampleOp( 100, 100)
    val image = ImageIO.read(is)
    val rescaled = op.filter(image, null)
    val baos = new ByteArrayOutputStream()
    ImageIO.write(rescaled, "PNG", baos)
    new ByteArrayInputStream(baos.toByteArray)
  }

}