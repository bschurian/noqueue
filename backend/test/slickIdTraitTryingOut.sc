

1 + 2
def funct():Boolean = {
  try{
    throw new IndexOutOfBoundsException
    false
  }catch{
    case exc: IndexOutOfBoundsException => true
  }
}
val x = funct()


/*import models.DB
import models.db.{DriverComponent, PK}
import org.mindrot.jbcrypt.BCrypt
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder
import slick.jdbc.SetParameter*/

//import scala.concurrent.ExecutionContext.Implicits.global


/*case class A (name:String, email:String)
trait withID {
  def id: Int
}
val aW = new A("asdf", "e@mail") with withID{def id= 3}
A.tupled("", "asdfa")
aW.id
val aWs = aW::List()*/

/*
trait AComponent {
  this: DriverComponent=>

  import driver.api._
  class ATable(tag: Tag) extends Table[A with withID](tag, "A") {

    def id = column[PK[A]]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME")
    def email = column[String]("EMAIL")
    def * = (name, email) <> (A.tupled, A.unapply)
  }

  val as = TableQuery[A]

  private val asAutoInc = as returning as.map(_.id)

  def insert(a: A): DBIO[A] = asAutoInc += a

  def getAnwenderById(id: PK[A]): DBIO[A] = as.filter(_.id === id).result.head
}*/

/*case class User(first: String, last: String = "e@example.com", id: Long = 0L)
//case class NewUser(first: String, last: String)

trait UserComponent {
  this: DriverComponent =>

  import driver.api._

  class Users(tag:Tag) extends Table[User](tag, "users") {

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def first = column[String]("first")

    def last = column[String]("last")

    def * = (id, first, last) <> (User.tupled, User.unapply _)

    //def autoInc = (first, last) <> (NewUser.tupled, NewUser.unapply _) returning id
  }

  val users = TableQuery[Users]

  val action = users += User("John", "Doe", 0L)
  //val id = exec(action)
}*/