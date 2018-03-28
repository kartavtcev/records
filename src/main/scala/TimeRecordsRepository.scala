import TimeRecordImplicits._
import slick.backend.DatabasePublisher
import slick.driver.H2Driver.api._

import scala.concurrent.Future

trait TimeRecordsRepository {
    def addRecords(records : Seq[TimeRecord]) : Future[Any] // Any is for general return which isn't used.
    /*
     If you have a streaming action, you can use db.stream instead of db.run to get a Reactive Streams Publisher instead of a Future.
     This allows data to be streamed asynchronously from the database with any compatible library like Akka Streams.
     Slick itself does not provide a full set of tools for working with streams but it has a .foreach utility method for consuming a stream:
    */
    def getAllBackdatingRecords : DatabasePublisher[TimeRecord] // emulate big data
    def init : Future[Any]
    def close : Unit
}

class TimeRecordRepositoryImpl(val db : Database) extends TimeRecordsRepository {
  val timeRecords = TableQuery[TimeRecords]
  val backdatingTimeRecordsQuery =
    sql"""select * from TIMERECORDS T1 where EXISTS
       (select * from TIMERECORDS T2
          where T2.ID < T1.ID AND T2.TIMESTAMP > T1.TIMESTAMP)""".as[TimeRecord]

  override def addRecords(records: Seq[TimeRecord]): Future[Any] = {
    db.run(timeRecords ++= records)
  }

  override def getAllBackdatingRecords: DatabasePublisher[TimeRecord] = { // addressing possibly large volume of data
    db.stream(backdatingTimeRecordsQuery
      .transactionally
      .withStatementParameters(fetchSize = 1000))
  }

  override def init: Future[Any] = {
    db.run(timeRecords.schema.create)
  }

  override def close: Unit = db.close()
}




