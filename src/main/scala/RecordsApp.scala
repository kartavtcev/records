import java.sql.Timestamp

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.Await

import slick.driver.H2Driver.api._

object RecordsApp extends App {

  val repo = new TimeRecordRepositoryImpl(Database.forConfig("h2mem1"))
  try {
    val f =
      repo.init
        .flatMap { _ =>
          repo.addRecords(
            Seq(TimeRecord(Timestamp.valueOf("2018-03-27 09:01:10")),
              TimeRecord(Timestamp.valueOf("2018-03-27 09:01:11")),
              TimeRecord(Timestamp.valueOf("2018-03-21 09:01:11")),
              TimeRecord(Timestamp.valueOf("2018-03-27 09:01:12")),
              TimeRecord(Timestamp.valueOf("2018-03-27 01:01:11")),
              TimeRecord(Timestamp.valueOf("2018-03-27 10:01:12"))
            ))
        }
        .flatMap { _ =>
          // The Publisher captures a Database plus a DBIO action.
          // The action does not run until you consume the stream.
          repo.getAllBackdatingRecords.foreach(println)
        }
    Await.result(f, Duration.Inf)
  } finally repo.close
}
