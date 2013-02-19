package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import java.sql.Clob
import play.api.Logger
import play.api.libs.Crypto
import java.security.MessageDigest
import scala.util.Random

/**
  * Default user type.
  */
case class Account(
  id: Pk[Long],
  email: String,
  name: String,
  password: String,
  permission: Permission)

object Account extends AbstractModel {
  
  val SEED_SIZE = 10

  def apply(email: String, name: String, password: String): Account =
    apply(NotAssigned, email, name, password, NormalUser)

  object Clob {
    def unapply(clob: Clob): Option[String] = Some(clob.getSubString(1, clob.length.toInt))
  }

  implicit val rowToPermission: Column[Permission] = {
    Column.nonNull[Permission] { (value, meta) =>
      Permission.fromString(value.toString) match {
        case Some(perm) => Right(perm)
        case None => Left(TypeDoesNotMatch(
          s"Cannot convert ${value} : ${value.getClass} to Permission for column ${meta.column}"))
      }
    }
  }

  // -- Parsers

  /**
    * Parse a User from a ResultSet
    */
  val simple: RowParser[Account] = {
      get[Pk[Long]]("user.id") ~
      str("user.email") ~
      str("user.name") ~
      str("user.password") ~
      get[Permission]("user.permission") map {
        case id ~ email ~ name ~ password ~ permission => Account(id, email, name, password, permission)
      }
  }

  // -- Queries

  /**
    * Retrieve a User from email.
    */
  def findByEmail(email: String): Option[Account] = {
    DB.withConnection {
      (implicit connection =>
        SQL("select * from user where email = {email}").on(
          'email -> email).as(Account.simple.singleOpt))
    }
  }

  /**
    * Retrieve all users.
    */
  def findAll: Seq[Account] = {
    DB.withConnection {
      (implicit connection =>
        SQL("select * from user").as(Account.simple *))
    }
  }

  /**
    * Authenticate a User.
    */
  def authenticate(email: String, password: String): Option[Account] = {
    Logger.debug(s"authenticating user ${email}")
    val sql = "select * from user where email = {email} and password = {password}"
    Logger("sql").debug(sql)
    DB.withConnection {
      (implicit connection =>
        SQL(sql).on(
            'email -> email,
            'password -> password).as(Account.simple.singleOpt))
    }
  }

  /**
   * Number of users in database
   */
  def userCount: Long =
    DB.withConnection(implicit connection =>
      SQL("select count(*) from user").as(scalar[Long].single))

  /**
    * Create a User.
    * @param email Email of the user
    * @param password Plain text password - is going to be stored as hash
    */
  def create(email: String, name: String, password: String, permission: Permission) = {
    val sql = 
        """
          insert into user(email,name,password,seed,active,date_created,permission) 
          values ({email},{name},{password},{seed},{active},{dateCreated},{permission})
        """
    Logger("sql").debug(sql)
    val hash: (String, String) = encodePassword(password)
    val id = DB.withConnection { implicit connection =>
      SQL(sql).on(
          'email -> email,
          'name -> name,
          'password -> hash._1,
          'seed -> hash._2,
          'active -> false,
          'permission -> permission.toString()
      ).executeInsert() match {
      	case Some(primaryKey: Long) => primaryKey
      	case _ => throw new RuntimeException("Could not insert into database, no PK returned")
      }
    }
    Logger.debug(s"User ${name} created with id ${id}")
    id
  }
  
  def encodePassword(password: String): (String, String) = {
    val seed: String = generateSeed
    val token = password + seed
    val hash = md5(token)
    (hash, seed)
  }
  
  def md5(s: String): String = {
    val m = MessageDigest.getInstance("MD5")
    m.update(s.getBytes(),0,s.length());
    new java.math.BigInteger(1,m.digest()).toString(16)
  }
  
  def verifyPassword( hash: String, password: String, seed: String) = {
    val token = password + seed
    val hashCalculated = md5(token)
    hashCalculated.equals(hash)
  }

  def generateSeed: String = new String(Random.alphanumeric.take(SEED_SIZE).toArray)
}
