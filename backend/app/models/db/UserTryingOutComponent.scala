package models.db

case class User(first: String, last: String = "e@example.com", id: Long = 0L)

trait UserTryingOutComponent {
  this: DriverComponent =>

  import driver.api._

  class Users(tag: Tag) extends Table[User](tag, "USERS") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def first = column[String]("NAME")
    def last = column[String]("EMAIL")

    def * = (last, first, id) <> (User.tupled, User.unapply)
  }

  val users = TableQuery[Users]
  val usersAutoInc = users returning users.map(_.id)

  def insert(user: User) = usersAutoInc += user
  def allUsers = users.take(20).result.head
  //val id = exec(action)
}
