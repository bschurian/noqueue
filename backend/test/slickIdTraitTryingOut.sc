case class Person(name: String, age: Int)
val persons: List[Person] =  List.apply(Person.apply("Alex", 30), Person("Bert", 20), Person("Clara", 25))
def compareAge(p1: Person, p2: Person): Boolean = p1.age < p2.age
def compareName(p1: Person, p2: Person): Boolean = p1.name > p2.name
val personsByAge = persons.sortWith(compareAge) //List(Person(Bert,20), Person(Clara,25), Person(Alex,30))
val personsByName = persons.sortWith(compareName) //List(Person(Clara,25), Person(Bert,20), Person(Alex,30))

val person1 = Person.apply("Alex", 30)
val nameP1 = person1.name// "Alex"
val person2 = person1.copy(age = 34)
val nameP2 = person2.name// "Alex"
val ageP2 = person2.age // 34
val eqPerson = person1.equals(person2) // false

import org.scalacheck._

Gen.choose(1,10).map(_*100).sample

(List(4,5,6)).apply(2)


/*1 + 2
def funct():Boolean = {
  try{
    throw new IndexOutOfBoundsException
    false
  }catch{
    case exc: IndexOutOfBoundsException => true
  }
}
val x = funct()*/


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