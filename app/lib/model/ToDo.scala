package lib.model

import ixias.model._
import ixias.util.EnumStatus
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping, nonEmptyText, text}

import java.time.LocalDateTime

// ユーザーを表すモデル
//~~~~~~~~~~~~~~~~~~~~
import ToDo._

case class ToDo(
  id: Option[Id],
  categoryId: Long @@ ToDoCategory,
  title: String,
  body: Option[String] = None,
  state: Short,
  updatedAt: LocalDateTime = NOW,
  createdAt: LocalDateTime = NOW
) extends EntityModel[Id]

// コンパニオンオブジェクト
//~~~~~~~~~~~~~~~~~~~~~~~~
object ToDo {

  val Id = the[Identity[Id]]
  type Id = Long @@ ToDo
  type WithNoId = Entity.WithNoId[Id, ToDo]
  type EmbeddedId = Entity.EmbeddedId[Id, ToDo]

  // ステータス定義
  //~~~~~~~~~~~~~~~~~
  sealed abstract class Status(val code: Short, val name: String)
    extends EnumStatus

  object States extends EnumStatus.Of[Status] {
    case object TODO extends Status(code = 0, name = "TODO(着手前)")
    case object DOING extends Status(code = 1, name = "進行中")
    case object DONE extends Status(code = 2, name = "完了")
  }

  // INSERT時のIDがAutoincrementのため,IDなしであることを示すオブジェクトに変換
  def apply(
    categoryId: Long @@ ToDoCategory,
    title: String,
    body: Option[String],
    state: Short
  ): WithNoId = {
    new Entity.WithNoId(
      new ToDo(
        id = None,
        categoryId = categoryId,
        title = title,
        body = body,
        state = state
        )
      )
  }

  case class ToDoFormData(title: String, body: String, categoryId: Long)
  val form = Form(
    mapping(
      "title" -> nonEmptyText(maxLength = 255),
      "body" -> text,
      "categoryId" -> longNumber
      )(ToDoFormData.apply)(ToDoFormData.unapply)
    )

}
