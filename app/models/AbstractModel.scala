package models

import anorm.ToStatement
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormat
import anorm.Column
import org.joda.time.DateTime
import anorm.MetaDataItem
import anorm.TypeDoesNotMatch

abstract class AbstractModel {
  
  val dateFormatGeneration: DateTimeFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmssSS");

  implicit def rowToDateTime: Column[DateTime] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case ts: java.sql.Timestamp => Right(new DateTime(ts.getTime))
      case d: java.sql.Date       => Right(new DateTime(d.getTime))
      case str: java.lang.String  => Right(dateFormatGeneration.parseDateTime(str))
      case _                      => Left(TypeDoesNotMatch("Cannot convert " + value + ":" + value.asInstanceOf[AnyRef].getClass))
    }
  }

  implicit val dateTimeToStatement = new ToStatement[DateTime] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: DateTime): Unit = {
      s.setTimestamp(index, new java.sql.Timestamp(aValue.withMillisOfSecond(0).getMillis()))
    }
  }

}
