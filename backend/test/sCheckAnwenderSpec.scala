import java.io.File

import models.{ Anwender, DB, UnregistrierterAnwender }
import models.db.{ AnwenderEntity, PK }
import org.scalacheck.Gen
import org.scalatest.Matchers._
import org.scalatest.prop.GeneratorDrivenPropertyChecks
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

  def anwEntityToModel(anwE: AnwenderEntity): Future[Anwender] = {
    for {
      regAnwE <- (new UnregistrierterAnwender(db)).registrieren(anwE)
    } yield new Anwender(db.dal.getAnwenderWithAdress(regAnwE.id.getOrElse(new PK[AnwenderEntity](2))), db) //TODO erase magic number
  }

  implicit val anwEGen = for {
    nE <- Gen.alphaStr
    pW <- Gen.alphaStr
    nN <- Gen.alphaStr
  } yield new AnwenderEntity(nE, pW, nN)

  val gAnwF: Gen[Future[Anwender]] = for {
    anwE <- anwEGen
    anw <- anwEntityToModel(anwE)
  } yield anw

  "Universal property - One that holds for all values" - {
    forAll { (a: String, b: String) =>
      a.length + b.length should equal((a + b).length)
    }
  }

  val hardCodedAnwEGen = Gen.oneOf((new AnwenderEntity("", "", "")) :: (new AnwenderEntity("a", "b", "c")) :: Nil)

  //anwEGen.
  //22.sd
  "Anwenders that were not persisted schould be IDless" - {
    forAll(anwEGen) { (anwE: AnwenderEntity) =>
      anwE.id == None
    }
  }

  "Anwenders that were persisted schould have IDless" - {
    forAll(gAnwF) { (anwF: Future[Anwender]) =>
      for {
        anw <- anwF
        (anwE: AnwenderEntity, _) <- anw.profilAnzeigen()
      } yield anwE.id != None
    }
  }
}
