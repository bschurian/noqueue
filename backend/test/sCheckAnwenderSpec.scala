import java.io.File

import models.{ Anwender, DB, UnregistrierterAnwender }
import models.db.{ AnwenderEntity, PK }
import org.h2.jdbc.JdbcSQLException
import org.scalacheck.Gen
import org.scalatest.Matchers._
import org.scalatest.prop.{ GeneratorDrivenPropertyChecks, Whenever }
import org.scalatest._
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by anwender on 19.07.2017.
 */
class sCheckAnwenderSpec extends AsyncFreeSpec with Matchers with GeneratorDrivenPropertyChecks {

  override def withFixture(test: NoArgAsyncTest): FutureOutcome = {
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
    /*
    "return his profile" in {
      for {
        profil <- anwender.profilAnzeigen()
      } yield (profil should equal((expectedAnwender, Some(expectedAdresse))))
    }
    "permit full-on-changing as long as nutzerName and nutzerEmail stay unique" in {
      val updateAnwender = anwenderize(12344321)
      for {
        updated <- anwender.anwenderInformationenAustauschen(updateAnwender, None)
        throwAway <- Future.successful(if (!updated) Failed)
        profil <- anwender.profilAnzeigen()
      } yield (profil should equal((updateAnwender.copy(id = expectedAnwender.id, password = expectedAnwender.password), None)))
    }
    "forbid full-on-changing if nutzerName and nutzerEmail are not unique" in {
      val updateAnwender = anwenderize(12344321)
      for {
        updated <- anwender.anwenderInformationenAustauschen(updateAnwender, None)
        throwAway <- Future.successful(if (!updated) Failed)
        profil <- anwender.profilAnzeigen()
      } yield (profil should equal((updateAnwender.copy(id = expectedAnwender.id, password = expectedAnwender.password), None)))
      val anw2 = new Anwender(db.dal.getAnwenderWithAdress(PK[AnwenderEntity](1L)), db)
      recoverToSucceededIf[JdbcSQLException](
        anw2.anwenderInformationenAustauschen(updateAnwender, None) map {
          updateHappened => assert(!updateHappened)
        }
      )
    }
    "permit partial changing as long as nutzerName and nutzerEmail stay unique" in {
      val updateAnwender = anwenderize(12344321)
      for {
        updated <- anwender.anwenderInformationenVeraendern(Some(updateAnwender.nutzerName), Some(updateAnwender.nutzerEmail), Some(None))
        throwAway <- Future.successful(if (!updated) Failed)
        profil <- anwender.profilAnzeigen()
      } yield (profil should equal((updateAnwender.copy(id = expectedAnwender.id, password = expectedAnwender.password), None)))
    }
    "forbid partial changing if nutzerName and nutzerEmail are not unique" in {
      val updateAnwender = anwenderize(12344321)
      for {
        updated <- anwender.anwenderInformationenVeraendern(Some(updateAnwender.nutzerName), Some(updateAnwender.nutzerEmail), Some(None))
        throwAway <- Future.successful(if (!updated) Failed)
        profil <- anwender.profilAnzeigen()
      } yield (profil should equal((updateAnwender.copy(id = expectedAnwender.id, password = expectedAnwender.password), None)))
      val anw2 = new Anwender(db.dal.getAnwenderWithAdress(PK[AnwenderEntity](1L)), db)
      recoverToSucceededIf[JdbcSQLException](
        anw2.anwenderInformationenVeraendern(Some(updateAnwender.nutzerName), Some(updateAnwender.nutzerEmail), Some(None)) map {
          updateHappened => assert(!updateHappened)
        }
      )
    }
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
    "return someone else's profile" in {
      for {
        profil <- anwender.anwenderAnzeigen(PK[AnwenderEntity](1L))
      } yield (profil should equal(AnwenderEntity("davidkaatz5@gmx.de", "$2a$10$LdM4yf7zgmjS8Pb5rGyeeeiUXFFc/wEJfeZloUPbjo8MD/CLA0B0S", "dkaatz5", None, Some(PK[AnwenderEntity](1)))))
    }
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
    "be able to book WsPs" in {
      val pw = "1234"
      val anw = anwenderize("newAnwenderComingIn")
      val unregistrierterAnwender = new UnregistrierterAnwender(db)
      for {
        newAnwenderE <- unregistrierterAnwender.registrieren(anw)
        newAnwender = new Anwender(db.dal.getAnwenderWithAdress(newAnwenderE.id.get), db)
        wsp <- newAnwender.wsFuerBestimmtenMitarbeiterBeitreten(expectedWsP.dienstLeistungId.value, expectedWsP.mitarbeiterId.value)
      } yield (wsp should equal(WarteschlangenPlatzEntity(None, newAnwenderE.id.get, expectedWsP.mitarbeiterId, expectedWsP.dienstLeistungId, None, None).copy(id = wsp.id)))
    }
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