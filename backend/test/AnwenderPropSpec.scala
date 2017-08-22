import java.io.File

import models.{ Anwender, DB, UnregistrierterAnwender }
import models.db.AnwenderEntity
import org.h2.jdbc.JdbcSQLException
import org.scalatest.{ FutureOutcome, Matchers, Outcome, PropSpec }
import org.scalatest._
import org.scalacheck.{ Gen, Prop }
import org.scalacheck.Gen.alphaStr
import org.scalacheck.Prop.{ collect, forAll }
import org.scalacheck.Prop.AnyOperators
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.util.Pretty
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import prop._
import utils.{ EmailAlreadyInUseException, NutzerNameAlreadyInUseException }

import scala.util.Try

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

  val myPreferredStringGen: Gen[String] = arbitrary[String]

  def anwEntityToModel(anwE: AnwenderEntity): Anwender = {
    try {
      Await.result(
        for {
          regAnwE <- (new UnregistrierterAnwender(db)).registrieren(anwE)
        } yield new Anwender(db.dal.getAnwenderWithAdress(regAnwE.id), db), 20 seconds
      )
    } catch {
      //in the case of a field that is not unique, replace the field and try again
      case eAExc: EmailAlreadyInUseException => {
        print("asdfasd" + anwE.toString)
        anwEntityToModel(anwE.copy(nutzerEmail = myPreferredStringGen.sample.get))
      }
      case nNAExc: NutzerNameAlreadyInUseException => {
        print("asdfasd" + anwE.toString)
        anwEntityToModel(anwE.copy(nutzerName = myPreferredStringGen.sample.get))
      }
    }
  }

  val anwEGen = for {
    nE <- myPreferredStringGen
    pW <- myPreferredStringGen
    nN <- myPreferredStringGen
  } yield new AnwenderEntity(nE, pW, nN)

  val anwGen: Gen[Anwender] = {
    for {
      anwE <- anwEGen
    } yield anwEntityToModel(anwE)
  }

  ignore("obligatorily false Prop") { //sanity check 1
    check(
      forAll { bool: Boolean =>
        false
      }
    )
  }
  ignore("obligatorily true Prop") {
    check(
      org.scalacheck.Prop.passed
    /*forAll { bool: Boolean =>
        1 == 1
      }*/
    )
  }

  ignore("unpersisted Anwenders") {
    check(
      forAll(anwEGen) { (anwE: AnwenderEntity) =>
        anwE.id.value == 0L
      }
    )
  }

  ignore("this fails") {
    check(
      forAll { xF: Int =>
        Await.result(
          Future(xF) map { x =>
            -1000 > x && x > 400
          }, 10 seconds
        )
      }
    )
  }

  ignore("this also fails but produces ~160 Lines of Stacktrace and I don't know why") {
    check(
      forAll { xF: Future[Int] =>
        Await.result(
          xF map { x =>
            -1000 > x && x > 400
          }, 10 seconds
        )
      }
    )
  }

  ignore("Anwenders that were persisted schould have IDs") {
    check(
      forAll(anwGen) { anw: Anwender =>
        Await.result(
          anw.anwender.map { anwE =>
            anwE.id.value > 0L
          }, 50 seconds
        )
      }
    )
  }

  /*
    "return his profile" in {
      for {
        profil <- anwender.profilAnzeigen()
      } yield (profil should equal((expectedAnwender, Some(expectedAdresse))))
    }*/

  property("Anwender permit full-on-changing as long as nutzerName and/or nutzerEmail stay unique") {
    check(
      forAll(anwGen, anwEGen) { (anwender, anwE) =>
        try {
          Await.result(
            for {
              (before, _) <- anwender.profilAnzeigen()
              updated <- anwender.anwenderInformationenAustauschen(anwE, None)
              (after, _) <- anwender.profilAnzeigen()
            } yield after == (anwE.copy(id = before.id, password = before.password)), 100 seconds
          )
        } catch {
          //in case Update doesn't go through
          case jdbc: JdbcSQLException => true
        }
      }
    )
  }

}
