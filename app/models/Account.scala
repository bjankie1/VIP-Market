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
import java.util.Date
import org.joda.time.DateTime

/**
  * Default user type.
  */
case class Account(
  id: Pk[Long],
  email: String,
  name: String,
  permission: Permission)

object Account extends AbstractModel {

  val SEED_SIZE = 10

  val TABLE_NAME = "user_account"

  def apply(email: String, name: String): Account =
    apply(NotAssigned, email, name, NormalUser)

  object Clob {
    def unapply(clob: Clob): Option[String] = Some(clob.getSubString(1, clob.length.toInt))
  }

  implicit val rowToPermission: Column[Permission] = {
    Column.nonNull[Permission] { (value, meta) =>
      Permission.fromString(value.toString) match {
        case Some(perm) => Right(perm)
        case None => Left(TypeDoesNotMatch(
          s"Cannot convert $value : ${value.getClass} to Permission for column ${meta.column}"))
      }
    }
  }

  // -- Parsers

  /**
    * Parse a User from a ResultSet
    */
  val simple: RowParser[Account] = {
      get[Pk[Long]](s"$TABLE_NAME.id") ~
      str(s"$TABLE_NAME.email") ~
      str(s"$TABLE_NAME.name") ~
      get[Permission](s"$TABLE_NAME.permission") map {
        case id ~ email ~ name  ~ permission => Account(id, email, name, permission)
      }
  }

  val hashSeedOnly: RowParser[(String,String)] = {
      str(s"$TABLE_NAME.password") ~
      str(s"$TABLE_NAME.seed") map {
        case password ~ seed => (password, seed)
      }
  }
  
  val idNameParser: RowParser[(Long,String)] = {
    get[Long](s"$TABLE_NAME.id") ~
    str(s"$TABLE_NAME.name") map {
      case id ~ name => (id, name)
    }
  }
  
  val withPasswordHash = simple ~ hashSeedOnly map {
    case user ~ hash => (user, hash)
  }
  
  // -- Queries

  /**
    * Retrieve a User from email.
    */
  def findByEmail(email: String): Option[Account] = {
    val sql = s"select * from $TABLE_NAME where email = {email}"
    Logger("sql").debug(sql)
    DB.withConnection {
      (implicit connection =>
        SQL(sql).on(
          'email -> email).as(Account.simple.singleOpt))
    }
  }

  def findById(id: Long) = {
    val sql = s"select * from $TABLE_NAME where id = {id}"
    DB.withConnection {
      (implicit connection =>
        SQL(sql).on(
          'id -> id).as(Account.simple.singleOpt))
    }
  }


  /**
    * Retrieve all users.
    */
  def findAll: Seq[Account] = {
    DB.withConnection {
      (implicit connection =>
        SQL(s"select * from $TABLE_NAME").as(Account.simple *))
    }
  }
  
  def idNameOnly(namePattern: String) = {
    val sql = s"""
      select id,name from $TABLE_NAME
      where name like {name} order by name
      """
    Logger("sql").debug(sql)
    DB.withConnection { (implicit connection =>
      SQL(sql).on(
        'name -> namePattern
      ).as(idNameParser *)
    )}
  }

  /**
    * Authenticate a User.
    */
  def authenticate(email: String, password: String): Option[Account] = {
    Logger.debug(s"authenticating user $email")
    val sql = s"select * from $TABLE_NAME where email = {email}"
    Logger("sql").debug(sql)
    val userOpt = DB.withConnection {
      (implicit connection =>
        SQL(sql).on('email -> email).as(withPasswordHash.singleOpt))
    }
    
    userOpt match {
      case Some((account,(hash, seed))) => {
        verifyPassword(hash, password, seed) match {
          case true  => Some(account)
          case false => None
        }
      }
      case _ => None
    }
  }

  /**
   * Number of users in database
   */
  def userCount: Long = {
    val sql = s"select count(*) from $TABLE_NAME"
    Logger("sql").debug(sql)
    DB.withConnection(implicit connection =>
      SQL(sql).as(scalar[Long].single))
  }

  /**
    * Create a User.
    * @param email Email of the user
    * @param password Plain text password - is going to be stored as hash
    */
  def create(email: String, name: String, password: String, permission: Permission) = {
    val sql = 
        s"""
          insert into $TABLE_NAME(email,name,password,seed,active,date_created,permission)
          values ({email},{name},{password},{seed},{active},{dateCreated},{permission})
        """
    Logger("sql").debug(sql)
    val hash: (String, String) = createPasswordHash(password)
    val id = DB.withConnection { implicit connection =>
      SQL(sql).on(
          'email -> email,
          'name -> name,
          'password -> hash._1,
          'seed -> hash._2,
          'active -> false,
          'dateCreated -> DateTime.now(),
          'permission -> permission.toString()
      ).executeInsert() match {
      	case Some(primaryKey: Long) => primaryKey
      	case _ => throw new RuntimeException("Could not insert into database, no PK returned")
      }
    }
    Logger.debug(s"User $name created with id ${id}")
    id
  }
  
  def createPasswordHash(password: String): (String, String) = {
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
