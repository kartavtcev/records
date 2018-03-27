import slick.driver.H2Driver.api._
import slick.jdbc.GetResult

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

import java.sql.Timestamp

case class TimeRecord(name: Timestamp, id: Option[Int] = None)
object MyImplicits {
  implicit val getTimeRecordResult = GetResult(r => TimeRecord(r.<<, r.<<))
}
import MyImplicits._

object CaseClassMapping extends App {

  // the base query for the TimeRecords table
  val timeRecords = TableQuery[TimeRecords]

  val db = Database.forConfig("h2mem1")

  def outOfOrderTimeRecords: DBIO[Seq[TimeRecord]] =
    sql"""select * from TIMERECORDS T1 where EXISTS
       (select * from TIMERECORDS T2
          where T2.ID > T1.ID AND T2.TIMESTAMP < T1.TIMESTAMP)""".as[TimeRecord]

  try {
    Await.result(db.run(DBIO.seq(
      // create the schema
      timeRecords.schema.create,

      // insert two TimeRecord instances
      timeRecords += TimeRecord(Timestamp.valueOf("2018-03-27 09:01:10")),
      timeRecords += TimeRecord(Timestamp.valueOf("2018-03-27 09:01:11")),
      timeRecords += TimeRecord(Timestamp.valueOf("2018-03-29 09:01:11")),
      timeRecords += TimeRecord(Timestamp.valueOf("2018-03-27 09:01:12")),
      timeRecords += TimeRecord(Timestamp.valueOf("2018-03-27 11:01:11")),
      timeRecords += TimeRecord(Timestamp.valueOf("2018-03-27 10:01:12")),



      //// print the TimeRecords (select * from TimeRecords)
      //timeRecords.result.map(println)
      outOfOrderTimeRecords.map(println)
    )), Duration.Inf)
  } finally db.close
}



class TimeRecords(tag: Tag) extends Table[TimeRecord](tag, "TIMERECORDS") {
  // Auto Increment the id primary key column
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  // The name can't be null
  def timestamp = column[Timestamp]("TIMESTAMP")
  // the * projection (e.g. select * ...) auto-transforms the tupled
  // column values to / from a TimeRecord
  //def * : ProvenShape[(Timestamp, Int)] = (timestamp, id)
  def * = (timestamp, id.?) <> (TimeRecord.tupled, TimeRecord.unapply)
}
