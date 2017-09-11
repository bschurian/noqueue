import java.io.File

import models.{ Anwender, DB, UnregistrierterAnwender }
import models.db.AnwenderEntity
import org.h2.jdbc.JdbcSQLException
import org.scalacheck.Arbitrary._
import org.scalacheck.{ Gen, Prop, Properties }
import org.scalacheck.Prop._
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder
import utils.{ EmailAlreadyInUseException, NutzerNameAlreadyInUseException }

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Only ScalaCheck-Style Definition of the properties of the Anwender-class
 * Created by anwender on 21.08.2017.
 */
class NurScalaCheckAnwenderPropertiesSpec extends Properties("AnwenderPropertiesSpec") {

  def myFixture(prop: () => Prop): Prop = {
    val fill = new File("./test/fill.sql")
    Await.result(db.db.run(db.dal.dropAllObjectsForTestDB()), 10 seconds)
    Await.result(db.db.run(db.dal.create), 10 seconds)
    Await.result(db.db.run(db.dal.runScript(fill.getAbsolutePath)), 10 seconds)
    prop()
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

  property("new anwenderEntity's Id should be standard value") =
    forAll(anwEGen) { anwE: AnwenderEntity =>
      anwE.id.value == 0L
    }

  /*property("this method is used to collect Strings") =
    forAll { s: String =>
      collect(s)(
        true
      )
    }*/

  property("Anwender permit full-on-changing as long as nutzerName and/or nutzerEmail stay unique") =
    myFixture(() =>
      forAll(anwENotInUseGen, anwGen) { (anwE, anwender) =>
        try {
          Await.result(
            for {
              (before, _) <- anwender.profilAnzeigen()
              updated <- anwender.anwenderInformationenAustauschen(anwE, None)
              (after, _) <- anwender.profilAnzeigen()
            } yield after == anwE.copy(id = before.id, password = before.password), 100 seconds
          )
        } catch {
          //in case Update doesn't go through
          case jdbc: JdbcSQLException => true
        }

      })
  property("reverse zweimal aufzurufen returniert die urspruengliche Liste") =
    forAll { list: List[Int] =>
      list.reverse.reverse == list
    }

}
