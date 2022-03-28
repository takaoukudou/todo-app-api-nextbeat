package json.reads

import play.api.libs.json.{Json, Reads}

case class JsValueOnlyIdCategory(
    id: Long
)
object JsValueOnlyIdCategory {
  implicit val reads: Reads[JsValueOnlyIdCategory] = Json.reads[JsValueOnlyIdCategory]
}
