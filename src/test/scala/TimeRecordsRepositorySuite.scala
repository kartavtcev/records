import java.sql.Timestamp

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import slick.jdbc.meta.MTable
import slick.driver.H2Driver.api._

class TimeRecordsRepositorySuite extends FunSuite with BeforeAndAfter with ScalaFutures {

  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))

  var db : Database = _
  var repo : TimeRecordsRepository = _

  def createSchema() =
    repo.init.futureValue
  
  def insertTimestampRecords(): Any =
    repo.addRecords(
      Seq(TimeRecord(Timestamp.valueOf("2018-03-27 09:01:10")))).futureValue

  before {
    db = Database.forConfig("h2mem1")
    repo = new TimeRecordRepositoryImpl(db) }

  test("Creating the Schema works on Repo init") {
    createSchema()

    val tables = db.run(MTable.getTables).futureValue

    assert(tables.size == 1)
    assert(tables.count(_.name.name.equalsIgnoreCase("TIMERECORDS")) == 1)
  }

  test("Inserting a TimeRecord works on Repo addRecords") {
    createSchema()

    val insertCount = insertTimestampRecords()
    assert(insertCount.asInstanceOf[Some[Int]].get == 1)
  }

  after { repo.close }
}
