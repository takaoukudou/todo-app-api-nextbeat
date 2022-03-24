package json.reads

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class JsValueCreateTodo(
    title:      String,
    body:       String,
    categoryId: Long
)

object JsValueCreateTodo {
  implicit val reads: Reads[JsValueCreateTodo] = Json.reads
}
