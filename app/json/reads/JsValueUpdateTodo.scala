package json.reads

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class JsValueUpdateTodo(
    title:      String,
    body:       String,
    categoryId: Long,
    state:      Short = 0
)

object JsValueUpdateTodo {
  implicit val reads: Reads[JsValueUpdateTodo] =
    ((__ \ "title").read[String] and
      (__ \ "body").read[String] and
      (__ \ "categoryId").read[Long] and
      (__ \ "state").read[Short])(JsValueUpdateTodo.apply _)
}
