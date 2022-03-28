/** to do sample project
  */

package controllers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import javax.inject._

@Singleton
class HomeController @Inject() (val controllerComponents: ControllerComponents) extends BaseController {

  def hello(): Action[AnyContent] = Action(Ok("Hello World"))

  def helloJson(): Action[AnyContent] = Action {
    val json: JsValue =
      Json.obj("hello" -> "world", "language" -> "scala")

    Ok(json)
  }
}
