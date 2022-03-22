package json.reads

import play.api.libs.json._

case class JsValueDeleteTodo(
    id: Long
)

object JsValueDeleteTodo {
  implicit val reads: Reads[JsValueDeleteTodo] = Json.reads[JsValueDeleteTodo]
}
