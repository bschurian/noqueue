import java.io.File
import java.sql.SQLException

import models.{ Anwender, DB, UnregistrierterAnwender }
import models.db.AnwenderEntity
import org.h2.jdbc.JdbcSQLException
import org.scalacheck.Gen
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.{ _ }
import org.scalatest.Matchers._
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest._
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder
import utils.{ EmailAlreadyInUseException, NutzerNameAlreadyInUseException }

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by anwender on 24.08.2017.
 */
class ScalaCheckBugs extends AsyncFreeSpec with Matchers with GeneratorDrivenPropertyChecks {

  override def withFixture(test: NoArgAsyncTest): FutureOutcome = {
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

  def anwEntityToModel(anwE: AnwenderEntity): Anwender = {
    try {
      Await.result(
        for {
          regAnwE <- new UnregistrierterAnwender(db).registrieren(anwE)
        } yield new Anwender(db.dal.getAnwenderWithAdress(regAnwE.id), db), 20 seconds
      )
    } catch {
      //in the case of a field that is not unique, replace the field and try again
      case eAExc: EmailAlreadyInUseException =>
        anwEntityToModel(anwE.copy(nutzerEmail = myPreferredStringGen.sample.get))

      case nNAExc: NutzerNameAlreadyInUseException =>
        anwEntityToModel(anwE.copy(nutzerName = myPreferredStringGen.sample.get))

    }
  }

  val myPreferredStringGen = arbitrary[String]

  val anwEGen = for {
    nE <- myPreferredStringGen
    pW <- myPreferredStringGen
    nN <- myPreferredStringGen
  } yield AnwenderEntity(nE, pW, nN)

  val anwENotInUseGen = for {
    nEmail <- {
      val id = posNum[Int].sample.get
      myPreferredStringGen.retryUntil(
        s => {
          println(id + " Email" + s)
          !Await.result(db.db.run(db.dal.existsEmail(s)), 100 seconds)
        }, 20
      )
    }
    pW <- myPreferredStringGen
    nName <- {
      val id = posNum[Int].sample.get
      myPreferredStringGen.retryUntil(
        s => {
          println(id + " Name " + s)
          !Await.result(db.db.run(db.dal.existsName(s)), 100 seconds)
        }, 20
      )
    }
  } yield AnwenderEntity(nEmail, pW, nName)

  val anwGen: Gen[Anwender] = {
    for {
      anwE <- anwEGen
    } yield anwEntityToModel(anwE)
  }

  //less testing
  val lessTests = Seq(PropertyCheckConfig(minSuccessful = 10))

  /*  "Sanity Checks" - {
    "obligatoryfailure" in {
      fail("Everything is good. Nothing to see here")
    }
  }*/

  "Errors in ScalaCheck" - {

    //range
    (1 to 10).map(_ => println("10MapSample: " + myPreferredStringGen.sample.getOrElse("asdfasdf")))
    val sGen = myPreferredStringGen.retryUntil(s => {
      println("sGen: (" + s + ")")
      false
    }, 10)

    /*"sGen Test" in {
      forAll(sGen) { s: String =>
        assert(true)
      }
    }*/
    val sGen2 = arbitrary[String] retryUntil ({ s =>
      println("s2: (" + s + ")")
      s.length >= 2
    }, 10)
    /*"sGen2 Test" in {
      forAll(sGen2) { s: String =>
        assert(s.length > 1)
      }
    }*/

    val oddGen = arbitrary[Int] retryUntil { x =>
      println("xOdd: (" + x + ")")
      x % 2 != 0
    }

    "oddIGen Test" in {
      forAll(oddGen) { x: Int =>
        assert(x % 2 != 0)
      }
    }

    "oddIGen2" in {
      forAll(anwEGen, oddGen) { (_, x) =>
        assert(x % 2 != 0)
      }
    }
    "that were not persisted schould be IDless" in {
      forAll(anwENotInUseGen) { anwE: AnwenderEntity =>
        anwE.id.value shouldEqual 0L
      }
    }

    "permit full-on-changing change nutzerName and email if the change goes through(paramorder reverse)" in {
      forAll(anwENotInUseGen, anwGen, MinSuccessful(10)) { (anwE, anwender) =>
        Await.result(
          for {
            (before, _) <- anwender.profilAnzeigen()
            updated <- anwender.anwenderInformationenAustauschen(anwE, None)
            (after, _) <- anwender.profilAnzeigen()
          } yield assert(!updated || (after == anwE.copy(id = before.id, password = before.password))), 10 seconds
        )
      }
    }
    "permit full-on-changing change nutzerName and email if the change goes through" in {
      assertThrows[RuntimeException] {
        forAll(anwGen, anwENotInUseGen, MinSuccessful(10)) { (anwender, anwE) =>
          Await.result(
            for {
              (before, _) <- anwender.profilAnzeigen()
              updated <- anwender.anwenderInformationenAustauschen(anwE, None)
              (after, _) <- anwender.profilAnzeigen()
            } yield assert(!updated || (after == anwE.copy(id = before.id, password = before.password))), 10 seconds
          )
        }
      }
    }
  }
}