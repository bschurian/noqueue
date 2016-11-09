/*package models
import org.joda.time.DateTime
import slick.driver.H2Driver.api._
import slick.lifted.{ProvenShape, ForeignKeyQuery}
// A Suppliers table with 6 columns: id, name, street, city, state, zip
class Adresses(tag: Tag)  extends Table[(Long, String, String, String, String)](tag, "ADRESSES") {
// This is the primary key column:
  def id: Rep[Long] = column[Long]("ADR_ID", O.PrimaryKey, O.AutoInc)
  def strasse: Rep[String] = column[String]("STRASSE")  def hausnummer: Rep[String] = column[String]("HAUSNUMMER")
  def plz: Rep[String] = column[String]("PLZ")  def stadt: Rep[String] = column[String]("STADT")
// Every table needs a * projection with the same type as the table's type parameter
  def * : ProvenShape[(Long, String, String, String, String)] =    (id, strasse, hausnummer, plz, stadt)
}
// A Coffees table with 5 columns: name, supplier id, price, sales, total
class Anwenders(tag: Tag)  extends Table[(Long, Long, String, String, String)](tag, "ANWENDER") {
  def id: Rep[Long] = column[Long]("ANW_ID", O.PrimaryKey, O.AutoInc)
def adresseID: Rep[Long] = column[Long]("ADR_ID")
def nutzerEmail: Rep[String] = column[String]("NUTZEREMAIL")
def password: Rep[String] = column[String]("PASSWORD")  def nutzerName: Rep[String] = column[String]("NUTZERNAME")
def * : ProvenShape[(Long, Long, String, String, String)] =    (id, adresseID, nutzerEmail, password, nutzerName)
// A reified foreign key relation that can be navigated to create a join
def adresse: ForeignKeyQuery[Adresses, (Long, String, String, String, String)] =    foreignKey("ANW_FK", adresseID, TableQuery[Adresses])(_.id)
}
// A Coffees table with 5 columns: name, supplier id, price, sales, total
class Leiters(tag: Tag)  extends Table[(Long, Long, Long)](tag, "LEITER") {
def id: Rep[Long] = column[Long]("LEI_ID", O.PrimaryKey, O.AutoInc)
def anwenderID: Rep[Long] = column[Long]("ANW_ID")
def anbieterID: Rep[Long] = column[Long]("ANB_ID")
def * : ProvenShape[(Long, Long, Long)] =    (id, anwenderID, anbieterID)
// A reified foreign key relation that can be navigated to create a join
def anwender: ForeignKeyQuery[Anwenders, (Long, Long, String, String, String)] =    foreignKey("ANW_FK", anwenderID, TableQuery[Anwenders])(_.id)
  def anbieter: ForeignKeyQuery[Anbieters, (Long, Long, String, String, String, Boolean, Int)] =    foreignKey("ANB_FK", anbieterID, TableQuery[Anbieters])(_.id)
}
// A Coffees table with 5 columns: name, supplier id, price, sales, total
class Anbieters(tag: Tag)  extends Table[(Long, Long, String, String, String, Boolean, Int)](tag, "ANBIETER") {
def id: Rep[Long] = column[Long]("ANB_ID", O.PrimaryKey, O.AutoInc)
def adresseID: Rep[Long] = column[Long]("ADR_ID")
def tel: Rep[String] = column[String]("TEL")
def oeffnungszeiten: Rep[String] = column[String]("OEFFNUNGSZEITEN")
def kontaktEmail: Rep[String] = column[String]("KONTAKTEMAIL")
def wsOffen: Rep[Boolean] = column[Boolean]("WSOFFEN")
def bewertung: Rep[Int] = column[Int]("BEWERTUNG")
def * : ProvenShape[(Long, Long, String, String, String, Boolean, Int)] =    (id, adresseID, tel, oeffnungszeiten, kontaktEmail, wsOffen, bewertung)
  // A reified foreign key relation that can be navigated to create a join
def adresse: ForeignKeyQuery[Adresses, (Long, String, String, String, String)] =    foreignKey("ADR_FK", adresseID, TableQuery[Adresses])(_.id)
}
// A Coffees table with 5 columns: name, supplier id, price, sales, total
class Bewertungs(tag: Tag)  extends Table[(Long, Long, Long, Int)] (tag, "BEWERTUNG") {
def id: Rep[Long] = column[Long]("BEW_ID", O.PrimaryKey, O.AutoInc)
def anbieterID: Rep[Long] = column[Long]("ANB_ID")
def anwenderID: Rep[Long] = column[Long]("ANW_ID")
def wert: Rep[Int] = column[Int]("WERT")
def * : ProvenShape[(Long, Long, Long, Int)] =    (id, anbieterID, anwenderID, wert)
// Reified foreign key relations that can be navigated to create a join
def anbieter: ForeignKeyQuery[Anbieters, (Long, Long, String, String, String, Boolean, Int)] =    foreignKey("ANB_FK", anbieterID, TableQuery[Anbieters])(_.id)
  def anwender: ForeignKeyQuery[Anwenders, (Long, Long, String, String, String)] =    foreignKey("ANW_FK", anwenderID, TableQuery[Anwenders])(_.id)
}
// A Coffees table with 5 columns: name, supplier id, price, sales, total
class Mitarbeiters(tag: Tag)  extends Table[(Long, Long, Long, Boolean)](tag,"MITARBEITER") {
  def id: Rep[Long] = column[Long]("MIT_ID", O.PrimaryKey, O.AutoInc)
  def anbieterID: Rep[Long] = column[Long]("ANB_ID")
  def anwenderID: Rep[Long] = column[Long]("ANW_ID")
  def anwesend: Rep[Boolean] = column[Boolean]("ANWESEND")
  def * : ProvenShape[(Long, Long, Long, Boolean)] =    (id, anbieterID, anwenderID, anwesend)
// Reified foreign key relations that can be navigated to create a join
def anbieter: ForeignKeyQuery[Anbieters, (Long, Long, String, String, String, Boolean, Int)] =    foreignKey("ANB_FK", anbieterID, TableQuery[Anbieters])(_.id)
  def anwender: ForeignKeyQuery[Anwenders, (Long, Long, String, String, String)] =    foreignKey("ANW_FK", anwenderID, TableQuery[Anwenders])(_.id)
}
// A Coffees table with 5 columns: name, supplier id, price, sales, total
class DienstleistungsTyps(tag: Tag)  extends Table[(Long, String)](tag,"DIENSTLEISTUNGSTYP") {
  def id: Rep[Long] = column[Long]("DLT_ID", O.PrimaryKey, O.AutoInc)
  def name: Rep[String] = column[String]("NAME")
  def * : ProvenShape[(Long, String)] =    (id, name)
}
class Dienstleistungs(tag: Tag)  extends Table[(Long, Long, Long, String, String)](tag,"DIENSTLEISTUNGEN") {
  def id: Rep[Long] = column[Long]("DL_ID", O.PrimaryKey, O.AutoInc)
  def dlTypID: Rep[Long] = column[Long]("DLT_ID")
  def anbieterID: Rep[Long] = column[Long]("ANB_ID")
  def kommentar: Rep[String] = column[String]("KOMMENTAR")
  def aktion: Rep[String] = column[String]("AKTION")
  def * : ProvenShape[(Long, Long, Long, String, String)] =    (id, dlTypID, anbieterID, kommentar, aktion)
// Reified foreign key relations that can be navigated to create a join
def dienstleistungsTyp: ForeignKeyQuery[DienstleistungsTyps, (Long, String)] =    foreignKey("ANW_FK", dlTypID, TableQuery[DienstleistungsTyps])(_.id)
  def anbieter: ForeignKeyQuery[Anbieters, (Long, Long, String, String, String, Boolean, Int)] =    foreignKey("ANB_FK", anbieterID, TableQuery[Anbieters])(_.id)
}
class WarteschlagenPlatzs(tag: Tag)  extends Table[(Long, Long, Long, Long, Long, DateTime, DateTime, Int)](tag,"DIENSTLEISTUNGSTYP") {
  def id: Rep[Long] = column[Long]("DLT_ID", O.PrimaryKey, O.AutoInc)
  def dienstleistungsID: Rep[Long] = column[Long]("DL_ID")
  def mitarbeiterID: Rep[Long] = column[Long]("MIT_ID")
  def anwenderID: Rep[Long] = column[Long]("ANW_ID")
  def folgePlatzID: Rep[Long] = column[Long]("FOLGEPLATZ_ID")
  def beginnZeitpunkt: Rep[DateTime] = column[DateTime]("BEGINN")
  def schaetzPunkt: Rep[DateTime] = column[DateTime]("Ende")
  def platzNummer: Rep[Int] = column[Int]("PLATZNUMMER")
  def * : ProvenShape[(Long, Long, Long, Long, Long, DateTime, DateTime, Int)] =    (id, dienstleistungsID, mitarbeiterID, anwenderID, folgePlatzID, beginnZeitpunkt, schaetzPunkt, platzNummer)  def dienstleistung: ForeignKeyQuery[Dienstleistungs, (Long, Long, Long, String, String)] =    foreignKey("DL_FK", dienstleistungsID, TableQuery[Dienstleistungs])(_.id)  def mitarbeiter: ForeignKeyQuery[Mitarbeiters, (Long, Long, Long, Boolean)] =    foreignKey("MIT_FK", mitarbeiterID, TableQuery[Mitarbeiters])(_.id)  def anwender: ForeignKeyQuery[Anwenders, (Long, Long, String, String, String)] =    foreignKey("ANW_FK", anwenderID, TableQuery[Anwenders])(_.id)  def folgePlatz: ForeignKeyQuery[WarteschlagenPlatzs, (Long, Long, Long, Long, Long, DateTime, DateTime, Int)] =    foreignKey("DL_FK", folgePlatzID, TableQuery[WarteschlagenPlatzs])(_.id)}class Tags(tag: Tag)  extends Table[(Long, Long, Long, String)](tag,"TAGS") {  def id: Rep[Long] = column[Long]("DLT_ID", O.PrimaryKey, O.AutoInc)  def anbieterID: Rep[Long] = column[Long]("ANB_ID")  def dienstleistungsID: Rep[Long] = column[Long]("DL_ID")  def name: Rep[String] = column[String]("TAG")  def * : ProvenShape[(Long, Long, Long, String)] =    (id, anbieterID, dienstleistungsID, name)  def anbieter: ForeignKeyQuery[Anbieters, (Long, Long, String, String, String, Boolean, Int)] =    foreignKey("ANB_FK", anbieterID, TableQuery[Anbieters])(_.id)  def dienstleistung: ForeignKeyQuery[Dienstleistungs, (Long, Long, Long, String, String)] =    foreignKey("DL_FK", dienstleistungsID, TableQuery[Dienstleistungs])(_.id)  def uniqueTag = index("A_has_Tag", (anbieterID, dienstleistungsID), unique = true)}class Faehigkeitens(tag: Tag)  extends Table[(Long, Long, Long, String)](tag,"FAEHIGKEITEN") {  def id: Rep[Long] = column[Long]("DLT_ID", O.PrimaryKey, O.AutoInc)  def mitarbeiterID: Rep[Long] = column[Long]("MIT_ID")  def dienstleistungsID: Rep[Long] = column[Long]("DL_ID")  def beschreibung: Rep[String] = column[String]("BESCHREIBUNG")  def * : ProvenShape[(Long, Long, Long, String)] =    (id, mitarbeiterID, dienstleistungsID, beschreibung)  def mitarbeiter: ForeignKeyQuery[Mitarbeiters, (Long, Long, Long, Boolean)] =    foreignKey("MIT_FK", mitarbeiterID, TableQuery[Mitarbeiters])(_.id)  def dienstleistung: ForeignKeyQuery[Dienstleistungs, (Long, Long, Long, String, String)] =    foreignKey("DL_FK", dienstleistungsID, TableQuery[Dienstleistungs])(_.id)
  def uniqueFaehigkeit = index("M_has_Faehigkeit", (mitarbeiterID, dienstleistungsID), unique = true)
}
*/ 