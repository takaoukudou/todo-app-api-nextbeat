package controllers

import json.reads.{JsValueCreateCategory, JsValueOnlyIdCategory}
import json.writes.JsValueCategoryItem
import lib.model.ToDoCategory
import lib.persistence.onMySQL
import model.ViewValueToDoCategory
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CategoryController @Inject() (val controllerComponents: ControllerComponents)(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  def list() = Action async { implicit request: Request[AnyContent] =>
    for {
      toDoCategories <- onMySQL.ToDoCategoryRepository.all()
    } yield {
      val JsValueCategoryItemList = toDoCategories.map(toDoCategory => {
        JsValueCategoryItem(
          ViewValueToDoCategory(
            toDoCategory.id,
            toDoCategory.v.name,
            toDoCategory.v.slug,
            toDoCategory.v.color
          )
        )
      })
      Ok(Json.toJson(JsValueCategoryItemList))
    }
  }

  def get(id: Long) = Action async { implicit request =>
    for {
      toDoCategoryOpt <- onMySQL.ToDoCategoryRepository.get(id.asInstanceOf[ToDoCategory.Id])
    } yield {
      toDoCategoryOpt match {
        case Some(toDoCategory) => {
          val jsValue = JsValueCategoryItem(
            ViewValueToDoCategory(
              toDoCategory.id,
              toDoCategory.v.name,
              toDoCategory.v.slug,
              toDoCategory.v.color
            )
          )
          Ok(Json.toJson(jsValue))
        }
        case _                  => NotFound(Json.obj("message" -> "not found"))
      }
    }
  }

  def store() = Action(parse.json) async { implicit request =>
    request.body
      .validate[JsValueCreateCategory]
      .fold(
        errors => {
          Future.successful(BadRequest(Json.obj("message" -> "validation error")))
        },
        categoryData => {
          for {
            _ <- onMySQL.ToDoCategoryRepository
                   .add(
                     ToDoCategory(
                       categoryData.name,
                       categoryData.slug,
                       categoryData.color
                     )
                   )
          } yield {
            Ok(Json.obj("message" -> "store completed"))
          }
        }
      )
  }

  def update(id: Long) = Action(parse.json) async { implicit request =>
    request.body
      .validate[JsValueCreateCategory]
      .fold(
        errors => {
          Future.successful(BadRequest(Json.obj("message" -> "validation error")))
        },
        categoryData => {
          for {
            oToDoCategory <- onMySQL.ToDoCategoryRepository.get(id.asInstanceOf[ToDoCategory.Id])
            result        <- {
              oToDoCategory match {
                case Some(toDoCategory) =>
                  onMySQL.ToDoCategoryRepository.update(
                    toDoCategory.map(
                      _.copy(
                        name  = categoryData.name,
                        slug  = categoryData.slug,
                        color = categoryData.color
                      )
                    )
                  )
                case None               => Future.successful(None)
              }
            }
          } yield {
            result match {
              case Some(_) => Ok(Json.obj("message" -> "update completed"))
              case _       => NotFound(Json.obj("message" -> "not found"))
            }
          }
        }
      )
  }

  def delete() = Action(parse.json) async { implicit request =>
    request.body
      .validate[JsValueOnlyIdCategory]
      .fold(
        errors => {
          Future.successful(BadRequest(Json.obj("message" -> "validation error")))
        },
        data => {
          for {
            result <- onMySQL.ToDoCategoryRepository.remove(data.id.asInstanceOf[ToDoCategory.Id])
          } yield {
            result match {
              case None => NotFound(Json.obj("message" -> "not found"))
              case _    => Ok(Json.obj("message" -> "delete completed"))
            }
          }
        }
      )
  }
}
