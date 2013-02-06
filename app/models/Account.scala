package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import java.sql.Clob

/**
  * Default user type.
  */
case class Account(
  id: String,
  email: String,
  name: String,
  password: String,
  permission: Permission)

object Account {

  def apply(id: String, email: String, name: String, password: String): Account =
    apply(id, email, name, password, NormalUser)

  object Clob {
    def unapply(clob: Clob): Option[String] = Some(clob.getSubString(1, clob.length.toInt))
  }

  implicit val rowToPermission: Column[Permission] = {
    Column.nonNull[Permission] { (value, meta) =>
      value match {
        case Clob("Administrator") => Right(Administrator)
        case Clob("NormalUser")    => Right(NormalUser)
        case _ => Left(TypeDoesNotMatch(
          "Cannot convert %s : %s to Permission for column %s".format(value, value.getClass, meta.column)))
      }
    }
  }

  // -- Parsers

  /**
    * Parse a User from a ResultSet
    */
  val simple: RowParser[Account] = {
    str("account.id") ~
      str("account.email") ~
      str("account.name") ~
      str("account.password") ~
      get[Permission]("account.permission") map {
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
    DB.withConnection {
      (implicit connection =>
        SQL(
          """
         select * from user where 
         email = {email} and password = {password}
        """).on(
            'email -> email,
            'password -> password).as(Account.simple.singleOpt))
    }
  }

  def userCount: Long =
    DB.withConnection(implicit connection =>
      SQL("select count(*) from user").as(scalar[Long].single))

  /**
    * Create a User.
    */
  def create(user: Account): Account = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into user values (
            {email}, {name}, {password}
          )
        """).on(
          'email -> user.email,
          'name -> user.name,
          'password -> user.password).executeUpdate()

      user

    }
  }

}
