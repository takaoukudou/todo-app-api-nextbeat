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
  implicit val reads: Reads[JsValueCreateTodo] =
    ((__ \ "title").read[String] and
      (__ \ "body").read[String] and
      (__ \ "categoryId").read[Long])(JsValueCreateTodo.apply _)
}
