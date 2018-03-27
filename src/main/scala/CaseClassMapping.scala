import slick.driver.H2Driver.api._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

import java.sql.Timestamp

object CaseClassMapping extends App {

  // the base query for the TimeRecords table
  val timeRecords = TableQuery[TimeRecords]

  val db = Database.forConfig("h2mem1")
  try {
    Await.result(db.run(DBIO.seq(
      // create the schema
      timeRecords.schema.create,

      // insert two TimeRecord instances
      timeRecords += TimeRecord(Timestamp.valueOf("2018-03-27 09:01:10")),
      timeRecords += TimeRecord(Timestamp.valueOf("2018-03-27 09:01:11")),

      // print the TimeRecords (select * from TimeRecords)
      timeRecords.result.map(println)
    )), Duration.Inf)
  } finally db.close
}

case class TimeRecord(name: Timestamp, id: Option[Int] = None)

class TimeRecords(tag: Tag) extends Table[TimeRecord](tag, "TimeRecords") {
  // Auto Increment the id primary key column
  def id = column[Int]("Id", O.PrimaryKey, O.AutoInc)
  // The name can't be null
  def timestamp = column[Timestamp]("Timestamp")
  // the * projection (e.g. select * ...) auto-transforms the tupled
  // column values to / from a TimeRecord
  def * = (timestamp, id.?) <> (TimeRecord.tupled, TimeRecord.unapply)
}
