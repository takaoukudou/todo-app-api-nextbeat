package json.reads

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class JsValueCreateCategory(
    name:  String,
    slug:  String,
    color: Short
)

object JsValueCreateCategory {
  implicit val reads: Reads[JsValueCreateCategory] = Json.reads
}
