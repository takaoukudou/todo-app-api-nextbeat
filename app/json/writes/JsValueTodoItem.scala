package json.writes

import model.ViewValueToDo
import play.api.libs.json.{Json, Writes}

case class JsValueTodoItem(
    id:          Long,
    title:       String,
    body:        String,
    stateStr:    String,
    categoryStr: String,
    color:       Short
)

object JsValueTodoItem {
  implicit val writes: Writes[JsValueTodoItem] = Json.writes[JsValueTodoItem]

  def apply(viewValueToDo: ViewValueToDo): JsValueTodoItem =
    JsValueTodoItem(
      id          = viewValueToDo.id,
      title       = viewValueToDo.title,
      body        = viewValueToDo.body.getOrElse(""),
      stateStr    = viewValueToDo.stateStr,
      categoryStr = viewValueToDo.categoryStr,
      color       = viewValueToDo.color
    )
}
