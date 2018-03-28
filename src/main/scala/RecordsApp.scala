import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.Await

import slick.driver.H2Driver.api._

import java.sql.Timestamp

object RecordsApp extends App {

  val repo = new TimeRecordRepositoryImpl(Database.forConfig("h2mem1"))
  try {
    val f =
      repo.init()
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
          repo.getAllBackdatingRecords().foreach(println)
        }
    Await.result(f, Duration.Inf)
  } finally repo.close()
}
