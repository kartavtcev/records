import java.sql.Timestamp

import slick.driver.H2Driver.api._
import slick.jdbc.GetResult

case class TimeRecord(timestamp: Timestamp, id: Option[Int] = None)

object TimeRecordImplicits {
  implicit val getTimeRecordResult = GetResult(r => TimeRecord(r.<<, r.<<))
}

class TimeRecords(tag: Tag) extends Table[TimeRecord](tag, "TIMERECORDS") {
  // Auto Increment the id primary key column
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  // The timestamp can't be null
  def timestamp = column[Timestamp]("TIMESTAMP")
  // the * projection (e.g. select * ...) auto-transforms the tupled
  // column values to / from a TimeRecord
  def * = (timestamp, id.?) <> (TimeRecord.tupled, TimeRecord.unapply)
}