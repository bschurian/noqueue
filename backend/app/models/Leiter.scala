package models

import java.sql.Timestamp
import java.util.NoSuchElementException

import models.db._
import slick.dbio.{ DBIO, DBIOAction }
import utils.UnauthorizedException

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by David on 29.11.16.
 */
class Leiter(val leiterAction: DBIO[(BetriebEntity, AnwenderEntity, LeiterEntity)]) extends Base {

  lazy val betrieb = leiterComposition map (_._1)

  lazy val anwender = leiterComposition map (_._2)

  lazy val leiter = leiterComposition map (_._3)

  lazy private val leiterComposition = db.run(leiterAction)

  /**
   * Ensures that the leiterAction is performed before calling the provided action
   *
   * Background:
   *
   * Since the LeiterModel is created without the need of performing the "leiterAction" given in the constructor
   * we need to ensure the authorization of all actions by calling leiterAction
   *
   * @param action database action to perform
   * @param betriebId the id of the Betrieb that is affected
   * @tparam T return type of database action
   * @return the result of the given action
   */
  private def authorizedAction[T](action: () => Future[T], betriebId: PK[BetriebEntity]): Future[T] =
    leiterComposition flatMap {
      case (betrieb, anwender, leiter) => if (betrieb.id.get == betriebId) action() else throw new UnauthorizedException
    } recover {
      case nse: NoSuchElementException => throw new UnauthorizedException
    }

  def betriebsInformationenVeraendern(id: PK[BetriebEntity], betrieb: BetriebEntity, adresse: AdresseEntity) =
    authorizedAction(() => db.run(dal.update(id, betrieb, adresse)), id)

  def mitarbeiterAnstellen(mitarbeiter: MitarbeiterEntity): Future[MitarbeiterEntity] =
    authorizedAction(() => db.run(dal.insert(mitarbeiter)), mitarbeiter.betriebId)

  def mitarbeiterEntlassen(mitarbeiterPK: PK[MitarbeiterEntity], betriebId: PK[BetriebEntity]): Future[Int] =
    authorizedAction(() => db.run(dal.deleteMitarbeiter(mitarbeiterPK, betriebId)), betriebId)

  def dienstleistungAnbieten(
    dienstleistungstypPK: PK[DienstleistungsTypEntity],
    name: String,
    dauer: Int,
    kommentar: String
  ): Future[DienstleistungEntity] = {
    throw new NotImplementedError("Not Implemented yet, fix todo")
    //@todo muss angepasst werden wir wollten die dienstleistungen mit mitarbeitern verknüpfen und nur indirekt
    //      über die Betriebe, es wir dalso eine Optionale MitarbeiterID übergeben und es wird die DL für den Mitarbeiter angelegt
    //      wenn keine Übergeben wird wird für jeden mitarbeiter die DL hinzugefügt.
    //
    //    for {
    //      betriebAndDLT <- Future.sequence(Seq(betrieb, db.run(dal.getDlTById(dienstleistungstypPK))))
    //      dienstleistung <- db.run(dal.insert(DienstleistungEntity(
    //        kommentar,
    //        "",
    //        betriebAndDLT(0).asInstanceOf[BetriebEntity].id.get,
    //        betriebAndDLT(1).asInstanceOf[DienstleistungsTypEntity].id.get
    //      )))
    //    } yield (dienstleistung)
  }

  def dienstleistungsInformationVeraendern(diensleistungPK: PK[DienstleistungEntity], dauer: Int, kommentar: String) = {
    //@todo implement me
    throw new NotImplementedError("Not implemented yet, implement it")
  }

  def dienstleistungEntfernen(dienstleistungPK: PK[DienstleistungEntity]) = {
    //@todo implement me
    throw new NotImplementedError("Not implemented yet, implement it")
  }

  def dienstleistungsTypErstellen(name: String) = {
    //@todo implement me
    throw new NotImplementedError("Not implemented yet, implement it")
  }

  def dlMitAktionAnbieten(dienstleistungPK: PK[DienstleistungEntity], aktion: String, von: Timestamp, bis: Timestamp) = {
    //@todo implement me
    throw new NotImplementedError("Not implemented yet, implement it")
  }

  def tagZuDLHinzufuegen(dienstleistungPK: PK[DienstleistungEntity], tag: String) = {
    //@todo implement me
    throw new NotImplementedError("Not implemented yet, implement it")
  }

  def tagAusDLEntfernen(dienstleistungPK: PK[DienstleistungEntity], tag: String) = {
    //@todo implement me
    throw new NotImplementedError("Not implemented yet, implement it")
  }
}
