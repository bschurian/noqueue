import java.io.File

import models.DB
import models.db.User
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{ AsyncFreeSpec, FreeSpec, Matchers }
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.streams.Streams

import scala.concurrent.ExecutionContext.Implicits
//import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

/**
 * Created by anwender on 15.08.2017.
 */
class optionlessIDv2Test extends AsyncFreeSpec with Matchers with GeneratorDrivenPropertyChecks {

  val awaitDuration: Duration = 1 seconds

  override def withFixture(test: NoArgAsyncTest) = { // Define a shared fixture
    try {
      // Shared setup (run at beginning of each test)
      val fill = new File("./test/fill.sql")
      //Awaiting  to ensure that db is fully cleaned up and filled  before test is started
      Await.result(db.db.run(db.dal.dropAllObjectsForTestDB()), 10 seconds)
      Await.result(db.db.run(db.dal.create), 10 seconds)
      Await.result(db.db.run(db.dal.runScript(fill.getAbsolutePath)), 10 seconds)
      Await.result(dbDb.run(dal.insert(User("Adam", "Adam@example.com"))), 10 seconds)
      test()
    } finally {
      // Shared cleanup (run at end of each test)
    }
  }

  val application = new GuiceApplicationBuilder()
    .in(Mode.Test)
    .build
  val db: DB = application.injector.instanceOf[DB]
  val dal = db.dal
  val dbDb = db.db
  //Await.result(dbDb.run(dal.create), awaitDuration)

  //lazy val res = dbDb.run(dal.insert(User("Adam", "Adam@example.com")))
  "Instered User" - {
    "id works" in {
      //res.map(x => x)
      assert(true)
    }
    "can be retrieved" in {
      dbDb.run(dal.allUsers)
        .map(_.first shouldEqual "Adam")
    }
    "test toString" in {
      dbDb.run(dal.insert(User("Bdam", "Bdam@example.com")))
        .map(_ shouldEqual (2L))
    }
    "unrelated Test of scala.concurrent.Future" - {
      "should Fail" in {
        Future.successful {
          fail
        }
      }
    }
  }
}
