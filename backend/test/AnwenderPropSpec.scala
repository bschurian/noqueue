import java.io.File

import models.{ Anwender, DB, UnregistrierterAnwender }
import models.db.AnwenderEntity
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll
import org.scalatest.{ FutureOutcome, Matchers, Outcome, PropSpec }
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest._
import org.scalacheck.Prop.AnyOperators
import prop._

/**
 * Created by anwender on 18.08.2017.
 */
class AnwenderPropSpec extends PropSpec with Matchers with Checkers {

  override def withFixture(test: NoArgTest): Outcome = {
    // Define a shared fixture
    try {
      // Shared setup (run at beginning of each test)
      val fill = new File("./test/fill.sql")
      //Awaiting  to ensure that db is fully cleaned up and filled  before test is started
      Await.result(db.db.run(db.dal.dropAllObjectsForTestDB()), 10 seconds)
      Await.result(db.db.run(db.dal.create), 10 seconds)
      Await.result(db.db.run(db.dal.runScript(fill.getAbsolutePath)), 10 seconds)
      test()
    } finally {
      // Shared cleanup (run at end of each test)
    }
  }

  val application = new GuiceApplicationBuilder()
    .in(Mode.Test)
    .build
  val db: DB = application.injector.instanceOf[DB]

  def anwEntityToModel(anwE: AnwenderEntity): Future[Anwender] = {
    for {
      regAnwE <- (new UnregistrierterAnwender(db)).registrieren(anwE)
    } yield new Anwender(db.dal.getAnwenderWithAdress(regAnwE.id), db)
  }

  implicit val anwEGen = for {
    nE <- Gen.alphaStr
    pW <- Gen.alphaStr
    nN <- Gen.alphaStr
  } yield new AnwenderEntity(nE, pW, nN)

  implicit val anwFGen: Gen[Future[Anwender]] = {
    for {
      anwE <- anwEGen
      anwender <- anwEntityToModel(anwE)
    } yield anwender
  }

  property("obligatorily false") {
    false
  }
  property("obligatorily assert false") {
    assert(false)
  }
  property("obligatorily false Prop") {
    check(forAll { bool: Boolean =>
      bool
    })
  }
  property("obligatorily true Prop") {
    check(
      org.scalacheck.Prop.passed
    /*forAll { bool: Boolean =>
        1 == 1
      }*/
    )
  }

  /*property("unpersisted Anwenders") {
    forAll(anwEGen) { (anwE: AnwenderEntity) =>
      anwE.id == 0L && false
    }
  }

  property("Anwenders that were persisted schould have IDless") {
    check(
      forAll(anwFGen) { anwF: Future[Anwender] =>
        Await.result(anwF flatMap { anw =>
          anw.anwender.map { anwE =>
            anwE.id.value == 123456L
          }
        }, 5 seconds)
      }
    )
  }*/

  /*
    "return his profile" in {
      for {
        profil <- anwender.profilAnzeigen()
      } yield (profil should equal((expectedAnwender, Some(expectedAdresse))))
    }*/

  /*property("Anwender permit full-on-changing as long as nutzerName and nutzerEmail stay unique") {
    forAll(anwFGen, anwEGen) { (anwenderF, anwE) =>
      Await.result(for {
        anwender <- anwenderF
        (before, _) <- anwender.profilAnzeigen()
        updated <- anwender.anwenderInformationenAustauschen(anwE, None)
        throwAway <- Future.successful(if (!updated) false)
        profil <- anwender.profilAnzeigen()
      } yield //(before should be(AnwenderEntity("afsd", "hgdfhdfh", "ssafsdfdfgdfd"))) //succeed
      // (1 should be(234))
      (profil._1 == (anwE.copy(id = before.id, password = before.password + "asdf"))), 10 seconds)
    }
  }*/

  /*property("reverse cancels itself out") {
    forAll { xs: List[Int] =>
      xs.reverse.reverse == xs
    }
  }*/

}
