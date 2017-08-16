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

  val application = new GuiceApplicationBuilder()
    .in(Mode.Test)
    .build
  val dbD: DB = application.injector.instanceOf[DB]
  val dal = dbD.dal
  val db = dbD.db
  Await.result(db.run(dal.create), awaitDuration)

  val res = db.run(dal.insert(User("Adam", "Adam@example.com")))
  "Instered User" - {
    "id works" in {
      //res.map(x => x)
      assert(true)
    }
    "can be retrieved" in {
      db.run(dal.allUsers)
        .map(_.first shouldEqual "Adam")
    }
    "test toString" in {
      res.map(_.toString shouldEqual "1")
      //assert(true)
    }
    "unrelated Test of scala.concurrent.Future"-{
      "should Fail" in {
        Future.successful {
          1 should be(1234)
        }
      }
    }
  }
}
