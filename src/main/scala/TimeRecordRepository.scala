import java.sql.Timestamp

import slick.backend.DatabasePublisher
import slick.driver.H2Driver
import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

import TimeRecordImplicits._

trait TimeRecordRepository {
    def addRecords(records : Seq[TimeRecord]) : Future[Any]
    def getAllBackdatingRecords() : DatabasePublisher[TimeRecord] //Future[Seq[TimeRecord]]
    def close() : Unit
}

class TimeRecordRepositoryImpl(val db : H2Driver.backend.Database) extends TimeRecordRepository {
  val timeRecords = TableQuery[TimeRecords]
  db.run(timeRecords.schema.create)

  def backdatingTimeRecordsQuery =
    sql"""select * from TIMERECORDS T1 where EXISTS
       (select * from TIMERECORDS T2
          where T2.ID < T1.ID AND T2.TIMESTAMP > T1.TIMESTAMP)""".as[TimeRecord]

  override def addRecords(records: Seq[TimeRecord]): Future[Any] = {
    db.run(DBIO.sequence(records.map(timeRecords += _)))
  }

  override def getAllBackdatingRecords(): DatabasePublisher[TimeRecord] = { // addressing possibly large volume of data
    db.stream(backdatingTimeRecordsQuery
      .transactionally
      .withStatementParameters(fetchSize = 1000))
      //.foreach(println)
      //.mapResult(r => r)
  }

  override def close(): Unit = db.close()
}



object TimeRecordRepositoryObj extends App {

  val repo = new TimeRecordRepositoryImpl(Database.forConfig("h2mem1"))
  try {
    val f = repo.addRecords(
      Seq(
        TimeRecord(Timestamp.valueOf("2018-03-27 09:01:10")),
        TimeRecord(Timestamp.valueOf("2018-03-27 09:01:11")),
        TimeRecord(Timestamp.valueOf("2018-03-21 09:01:11")),
        TimeRecord(Timestamp.valueOf("2018-03-27 09:01:12")),
        TimeRecord(Timestamp.valueOf("2018-03-27 01:01:11")),
        TimeRecord(Timestamp.valueOf("2018-03-27 10:01:12"))
      ))
      .flatMap { _ =>
        repo.getAllBackdatingRecords().foreach(println)
      }
    Await.result(f, Duration.Inf)
  } finally repo.close()


  /*
  // the base query for the TimeRecords table
  val timeRecords = TableQuery[TimeRecords]

  val db = Database.forConfig("h2mem1")

  def backdatingTimeRecordsQuery =
    sql"""select * from TIMERECORDS T1 where EXISTS
       (select * from TIMERECORDS T2
          where T2.ID < T1.ID AND T2.TIMESTAMP > T1.TIMESTAMP)""".as[TimeRecord]

  try {
    val f = db.run(DBIO.seq(
      // create the schema
      timeRecords.schema.create,

      // insert two TimeRecord instances
      timeRecords += TimeRecord(Timestamp.valueOf("2018-03-27 09:01:10")),
      timeRecords += TimeRecord(Timestamp.valueOf("2018-03-27 09:01:11")),
      timeRecords += TimeRecord(Timestamp.valueOf("2018-03-21 09:01:11")),
      timeRecords += TimeRecord(Timestamp.valueOf("2018-03-27 09:01:12")),
      timeRecords += TimeRecord(Timestamp.valueOf("2018-03-27 01:01:11")),
      timeRecords += TimeRecord(Timestamp.valueOf("2018-03-27 10:01:12"))
    )).flatMap { _ =>
      db.stream(backdatingTimeRecordsQuery
                  .transactionally
                  .withStatementParameters(fetchSize = 1000))
            //.foreach(println) // addressing possibly large volume of data
        .mapResult(_)
    }
    Await.result(f, Duration.Inf)
  } finally db.close
  */
}




