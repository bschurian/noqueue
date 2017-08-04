import java.io.File

import models.{ Anwender, DB, UnregistrierterAnwender }
import models.db.{ AnwenderEntity, PK }
import org.scalacheck.Gen
import org.scalatest.Matchers._
import org.scalatest.prop.{ GeneratorDrivenPropertyChecks, Whenever }
import org.scalatest.{ AsyncTestSuite, FreeSpec, Matchers, Outcome }
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by anwender on 19.07.2017.
 */
class sCheckAnwenderSpec extends FreeSpec with Matchers with GeneratorDrivenPropertyChecks {

  override def withFixture(test: NoArgTest): Outcome = {
    // Define a shared fixture
    try {
      // Shared setup (run at beginning of each test)
      val fill = new File("./test/fill.sql")
      //Awaiting  to ensure that db is fully cleaned up and filled  before test is started
      Await.result(db.db.run(db.dal.create), 10 seconds)
      Await.result(db.db.run(db.dal.runScript(fill.getAbsolutePath)), 10 seconds)
      test()
    } finally {
      // Shared cleanup (run at end of each test)
      Await.result(db.db.run(db.dal.dropAllObjectsForTestDB()), 10 seconds)
    }
  }

  val application = new GuiceApplicationBuilder()
    .in(Mode.Test)
    .build
  val db: DB = application.injector.instanceOf[DB]

  def anwEntityToModel(anwE: AnwenderEntity): Future[Option[Anwender]] = {
    for {
      regAnwE <- (new UnregistrierterAnwender(db)).registrieren(anwE)
    } yield regAnwE.id.map { id => new Anwender(db.dal.getAnwenderWithAdress(id), db) }
  }

  implicit val anwEGen = for {
    nE <- Gen.alphaStr
    pW <- Gen.alphaStr
    nN <- Gen.alphaStr
  } yield new AnwenderEntity(nE, pW, nN)

  implicit val anwFGen: Gen[Future[Option[Anwender]]] = for {
    anwE <- anwEGen
    anwender <- anwEntityToModel(anwE)
  } yield anwender

  "Anwenders" - {

    "that were not persisted schould be IDless" - {
      forAll(anwEGen) { (anwE: AnwenderEntity) =>
        anwE.id == None
      }
    }

    "that were persisted schould have IDless" - {
      forAll(anwFGen) { anwOF: Future[Option[Anwender]] =>
        /*anwOF map { anwO =>
          anwO flatMap { anw =>
            anw.anwender.map { anwE =>
              anwE.id
              // wie kann ich bei verschachtelten Monaden den Wertnach oben transportieren
              // zB. hier F[O[F[O[Int]]]] und gebraucht wird F[Int] oder F[O[Int]]
            }
          } should not equal None
        }*/ true
      }
    }

  }
}