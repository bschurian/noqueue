//package models.db
//import org.mindrot.jbcrypt.BCrypt
//import slick.jdbc.SetParameter
//
//import play.api.libs.concurrent.Execution.Implicits.defaultContext
//
///**
// * Created by anwender on 14.08.2017.
// */
//
///**
// * AnwenderEntity Component Trait including Driver and AdresseEntity Component traits via cake pattern injection
// */
//trait AnwenderOptionLessComponent {
//  this: DriverComponent with AdresseComponent with BetriebComponent with MitarbeiterComponent with LeiterComponent =>
//
//  import driver.api._
//
//  /**
//   * AnwenderEntity Table Schema definition
//   *
//   * @param tag
//   */
//  class AnwenderOptionLessIdTable(tag: Tag) extends Table[AnwenderOptionLessIdEntity](tag, "ANWENDER") {
//
//    def id = column[PK[AnwenderOptionLessIdEntity]]("ID", O.PrimaryKey, O.AutoInc)
//    def nutzerName = column[String]("NUTZERNAME")
//    //    def nutzerEmail = column[String]("NUTZEREMAIL")
//    //    def password = column[String]("PASSWORD")
//    //    def adresseId = column[Option[PK[AdresseEntity]]]("ADRESSE_ID")
//    //    def adresse = foreignKey("fk_adresse", adresseId, adresses)(_.id.?)
//    //    def nameUnique = index("nameUnique", nutzerName, unique = true)
//    //    def emailUnique = index("emailUnique", nutzerEmail, unique = true)
//
//    /**
//     * Default Projection Mapping to case Class
//     * @return
//     */
//    def * = (id, nutzerName) <> (AnwenderOptionLessIdEntity.tupled, AnwenderOptionLessIdEntity.unapply)
//  }
//
//  val anwenders = TableQuery[AnwenderOptionLessIdTable]
//
//  private val anwenderAutoInc = anwenders returning anwenders.map(_.id)
//
//  def insert(anwender: AnwenderOptionLessIdEntity): DBIO[AnwenderOptionLessIdEntity] = anwenderAutoInc += anwender
//
//  def getAnwenderById(id: PK[AnwenderOptionLessIdEntity]): DBIO[AnwenderOptionLessIdEntity] = anwenders.filter(_.id === id).result.head
//
//  //  def getAnwenderByName(name: String): DBIO[AnwenderEntity] = anwenders.filter(_.nutzerName === name).result.head
//  //
//  //  def listAnwender(page: Int, size: Int): DBIO[Seq[AnwenderEntity]] = anwenders.sortBy(_.nutzerName).drop(page * size).take(size).result
//  //
//  //  def searchAnwender(query: String, page: Int, size: Int): DBIO[Seq[AnwenderEntity]] = {
//  //    val tokenizedQuery = Option("%" + query + "%")
//  //    anwenders.filter {
//  //      (a: AnwenderTable) =>
//  //        List(
//  //          tokenizedQuery.map(a.nutzerName.like(_)),
//  //          tokenizedQuery.map(a.nutzerEmail.like(_))
//  //        ).collect({ case Some(c) => c }).reduceLeftOption(_ || _).getOrElse(true: Rep[Boolean])
//  //    }.sortBy(_.nutzerName).drop(page * size).take(size).result
//  //
//  //  }
//  //
//  //  /**
//  //    * full update of an AnwenderEntity (currently only updates the nutzerName and the nutzerEmail)
//  //    * @param id
//  //    * @param anwenderEntity
//  //    * @return
//  //    */
//  //  def update(id: PK[AnwenderEntity], anwenderEntity: AnwenderEntity): DBIO[Int] =
//  //  anwenders.filter(_.id === id)
//  //    .map(anw => (anw.nutzerName, anw.nutzerEmail, anw.adresseId)).update((anwenderEntity.nutzerName, anwenderEntity.nutzerEmail, anwenderEntity.adresseId))
//}
//
