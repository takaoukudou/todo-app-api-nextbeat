package json.writes

import model.ViewValueToDoCategory
import play.api.libs.json.{Json, Writes}

case class JsValueCategoryItem(
    id:    Long,
    name:  String,
    slug:  String,
    color: Short
)

object JsValueCategoryItem {
  implicit val writes: Writes[JsValueCategoryItem] = Json.writes[JsValueCategoryItem]

  def apply(viewValueToDoCategory: ViewValueToDoCategory): JsValueCategoryItem =
    JsValueCategoryItem(
      id    = viewValueToDoCategory.id,
      name  = viewValueToDoCategory.name,
      slug  = viewValueToDoCategory.slug,
      color = viewValueToDoCategory.color
    )
}
