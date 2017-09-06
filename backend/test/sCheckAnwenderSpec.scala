import java.io.File
import java.sql.SQLException

import models.{ Anwender, DB, UnregistrierterAnwender }
import models.db.{ AnwenderEntity, PK }
import org.h2.jdbc.JdbcSQLException
import org.scalacheck.Gen
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.Matchers._
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest._
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder
import utils.{ EmailAlreadyInUseException, NutzerNameAlreadyInUseException, WspDoesNotExistException }

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * ScalaCheck-like properties of the Anwender-class using ScalaTest Features like assertThrows and Matchers
 * Created by anwender on 19.07.2017.
 */
class sCheckAnwenderSpec extends AsyncFreeSpec with Matchers with GeneratorDrivenPropertyChecks {

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
    nEmail <- myPreferredStringGen.retryUntil(s => !Await.result(db.db.run(db.dal.existsEmail(s)), 100 seconds), 20)
    pW <- myPreferredStringGen
    nName <- myPreferredStringGen.retryUntil(s => !Await.result(db.db.run(db.dal.existsName(s)), 100 seconds), 20)
  } yield AnwenderEntity(nEmail, pW, nName)

  val anwGen: Gen[Anwender] = {
    for {
      anwE <- anwEGen
    } yield anwEntityToModel(anwE)
  }

  "Anwenders" - {

    "that were not persisted schould be IDless" in {
      forAll(anwEGen) { anwE: AnwenderEntity =>
        anwE.id.value shouldEqual 0L
      }
    }

    "that were persisted schould have IDs higher than the standard ID" in {
      forAll(anwGen) { anw: Anwender =>
        Await.result(
          anw.anwender.map { anwE =>
            assert(anwE.id.value > 0L)
          }, 10 seconds
        )
      }
    }
    /*
    "return his profile" in {
      for {
        profil <- anwender.profilAnzeigen()
      } yield (profil should equal((expectedAnwender, Some(expectedAdresse))))
    }*/
    "permit full-on-changing change nutzerName and email if the change goes through" in {
      forAll(anwENotInUseGen, anwGen) { (anwE, anwender) =>
        Await.result(
          for {
            (before, _) <- anwender.profilAnzeigen()
            updated <- anwender.anwenderInformationenAustauschen(anwE, None)
            (after, _) <- anwender.profilAnzeigen()
          } yield assert(!updated || (after == anwE.copy(id = before.id, password = before.password))), 10 seconds
        )
      }
    }
    def somethingNotUniqueProperty(makeOtherUniquesUnique: (AnwenderEntity) => AnwenderEntity, myAssertT: (=> Any) => Assertion): Assertion = {
      forAll(anwENotInUseGen, anwGen, anwGen) { (anwE1, anwender1, anwender2) =>
        //val anwE2 = makeOtherUniquesUnique(anwE1)
        myAssertT { () =>
          val anwE2 = makeOtherUniquesUnique(anwE1)
          Await.result(
            for {
              _ <- anwender1.anwenderInformationenAustauschen(anwE1, None)
              _ <- anwender2.anwenderInformationenAustauschen(anwE2, None)
            } yield false, 20 seconds
          )
        }
      }
    }

    "forbid full-on-changing if nutzerEmail is not unique" in {
      forAll(anwENotInUseGen, anwGen, anwGen) { (anwE1, anwender1, anwender2) =>
        assertThrows[EmailAlreadyInUseException] {
          val anwE2 = anwE1.copy(nutzerName = " pre" + anwE1.nutzerName + "SomethingExtra")
          Await.result(
            for {
              _ <- anwender1.anwenderInformationenAustauschen(anwE1, None)
              _ <- anwender2.anwenderInformationenAustauschen(anwE2, None)
            } yield false, 20 seconds
          )
        }
      }
    }

    "forbid full-on-changing if nutzerName is not unique" in {
      forAll(anwENotInUseGen, anwGen, anwGen) { (anwE1, anwender1, anwender2) =>
        assertThrows[NutzerNameAlreadyInUseException] {
          val anwE2 = anwE1.copy(nutzerEmail = anwE1.nutzerEmail + "SomethingExtra")
          Await.result(
            for {
              _ <- anwender1.anwenderInformationenAustauschen(anwE1, None)
              _ <- anwender2.anwenderInformationenAustauschen(anwE2, None)
            } yield false, 20 seconds
          )
        }
      }
    }

    /*
    "be able to search for DiensleistungsTyps" in {
      anwender.dienstleistungsTypSuchen("Haare", 0, 10) map {
        seq =>
        {
          //assert(seq contains())
          seq.length should equal(2)
        }
      }
    }
    "be able to change the password" in {
      val pw = "1234"
      val anw = anwenderize("newAnwenderComingIn")
      val unregistrierterAnwender = new UnregistrierterAnwender(db)
      for {

        newAnwenderE <- unregistrierterAnwender.registrieren(anw)
        newAnwender <- Future.successful(new Anwender(db.dal.getAnwenderWithAdress(newAnwenderE.id.get), db))
        pwChanged <- newAnwender.passwordVeraendern(anw.password, pw)
        throwAway <- Future.successful(if (!pwChanged) Failed)
        profil <- newAnwender.profilAnzeigen()
      } yield (assert(BCrypt.checkpw(pw, profil._1.password)))
    }
    "not be able to if old password is not given" in {
      val pw = "password1234"
      anwender.passwordVeraendern("notTheRealPassword", pw) map {
        changerPW => assert(!changerPW)
      }
    }
    "be able to search Anwender with a query relating to email" in {
      anwender.anwenderSuchen(Some("davidkaatz"), 0, 100) map {
        seq =>
        {
          assert(seq.exists(_.id.get == PK[AnwenderEntity](3L)))
          seq.length should equal(2)
        }
      }
    }
    "be able to search Anwender with a query relating to nutzerName" in {
      anwender.anwenderSuchen(Some("dkaatz"), 0, 100) map {
        seq =>
        {
          assert(seq.exists(_.id.get == PK[AnwenderEntity](3L)))
          seq.length should equal(2)
        }
      }
    }
    "be able to search Anwender without a query" in {
      anwender.anwenderSuchen(None, 0, 100) map {
        seq =>
        {
          assert(seq.exists(_.id.get == PK[AnwenderEntity](3L)))
          seq.length should equal(18)
        }
      }
    }
    */ "return someone else's profile" in {
      forAll(anwGen) { anwender =>
        Await.result(for {
          profil <- anwender.anwenderAnzeigen(PK[AnwenderEntity](1L))
        } yield (profil should equal(AnwenderEntity("davidkaatz5@gmx.de", "$2a$10$LdM4yf7zgmjS8Pb5rGyeeeiUXFFc/wEJfeZloUPbjo8MD/CLA0B0S", "dkaatz5", None, PK[AnwenderEntity](1)))), 10 seconds)
      }
    }

    "have no WarteschlangePlatz (an empty queue)" in {
      forAll(anwGen) { anwender =>
        assertThrows[WspDoesNotExistException](
          try {
            Await.result(anwender.wspAnzeigen(), 10 seconds)
          } catch {
            case (e: java.util.NoSuchElementException) =>
              e.getStackTrace.map(println(_))
              throw e
          }
        )
      }
    }
    /*
        "be able to show a WarteschlangePlatz" in {
      for {
        wsp <- anwender.wspAnzeigen()
      } yield (wsp._1 shouldEqual (expectedWsP.id.get))
    }
    "be able to cancel a WarteschlangePlatz" in {
      for {
        a <- anwender.wsVerlassen()
        b <- anwender.wspAnzeigen() //@ todo FIX
      } yield (b._1)
      succeed
    }
    "be able to see every Betrieb Anwender is in relation with" in {
      anwender.meineBetriebe() map {
        seq =>
        {
          assert(seq.exists(_._1.betriebEntity.id.get == PK[BetriebEntity](8)))
          seq.length should equal(5)
        }
      }
    }
    "be able to create a Betrieb" in {
      val betriebE = BetriebEntity("Laden", "030-1234567", "first cry of the rooster-sundown", "Laden@example.com", PK[AdresseEntity](8))
      for {
        (betrieb, adresse) <- anwender.betriebErstellen(betriebE, expectedAdresse)
        persistedBetrieb <- anwender.betriebAnzeigen(betrieb.id.get) map (_._1)
      } yield (persistedBetrieb shouldEqual (betriebE.copy(id = persistedBetrieb.id)))
    }
    */

    /*"be able to book WsPs" in {
      forAll(anwGen){anwender

      }
      val pw = "1234"
      val anw = anwenderize("newAnwenderComingIn")
      val unregistrierterAnwender = new UnregistrierterAnwender(db)
      for {
        newAnwenderE <- unregistrierterAnwender.registrieren(anw)
        newAnwender = new Anwender(db.dal.getAnwenderWithAdress(newAnwenderE.id.get), db)
        wsp <- newAnwender.wsFuerBestimmtenMitarbeiterBeitreten(expectedWsP.dienstLeistungId.value, expectedWsP.mitarbeiterId.value)
      } yield (wsp should equal(WarteschlangenPlatzEntity(None, newAnwenderE.id.get, expectedWsP.mitarbeiterId, expectedWsP.dienstLeistungId, None, None).copy(id = wsp.id)))
    }*/

    /*
    "be able to get next time of a Betrieb" in {
      /*INSERT INTO "MITARBEITER" ("ANWESEND", "BETR_ID", "ANW_ID", "MIT_ID") VALUES (true, 8, 3, 3);
  INSERT INTO "MITARBEITER" ("ANWESEND", "BETR_ID", "ANW_ID", "MIT_ID") VALUES (true, 8, 1, 4);
  INSERT INTO "MITARBEITER" ("ANWESEND", "BETR_ID", "ANW_ID", "MIT_ID") VALUES (true, 8, 4, 5);*/
      for {
        seq <- anwender.getNextTimeSlotsForBetrieb(PK[BetriebEntity](8).value)
      } yield (2) //@todo
      succeed
    }
 */
  }
}