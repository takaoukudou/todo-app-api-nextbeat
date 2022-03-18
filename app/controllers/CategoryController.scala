package controllers

import json.writes.JsValueCategoryItem
import lib.persistence.onMySQL
import model.ViewValueToDoCategory
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._
import scala.concurrent.ExecutionContext

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
  //
  //  def register() = Action async { implicit request: Request[AnyContent] =>
  //    for {
  //      _ <- onMySQL.ToDoCategoryRepository.all()
  //    } yield {
  //      Ok(views.html.category.Store(ViewValueToDoCategory.form))
  //    }
  //  }
  //
  //  /** 登録処理実を行う
  //    */
  //  def store() = Action async { implicit request: Request[AnyContent] =>
  //    ViewValueToDoCategory.form
  //      .bindFromRequest()
  //      .fold(
  //        // 処理が失敗した場合に呼び出される関数
  //        (formWithErrors: Form[ToDoCategoryFormData]) => {
  //          Future.successful(BadRequest(views.html.category.Store(formWithErrors)))
  //        },
  //        // 処理が成功した場合に呼び出される関数
  //        (toDoCategoryFormData: ToDoCategoryFormData) => {
  //          for {
  //            // データを登録。returnのidは不要なので捨てる
  //            _ <- onMySQL.ToDoCategoryRepository
  //                   .add(
  //                     ToDoCategory(
  //                       toDoCategoryFormData.name,
  //                       toDoCategoryFormData.slug,
  //                       toDoCategoryFormData.color
  //                     )
  //                   )
  //          } yield {
  //            Redirect(routes.CategoryController.list())
  //          }
  //        }
  //      )
  //  }
  //
  //  def edit(id: Long) = Action async { implicit request: Request[AnyContent] =>
  //    for {
  //      toDoCategory <- onMySQL.ToDoCategoryRepository.get(id.asInstanceOf[ToDoCategory.Id])
  //    } yield {
  //      toDoCategory match {
  //        case Some(toDoCategory) =>
  //          Ok(
  //            views.html.category.Edit(
  //              toDoCategory.v.id.getOrElse(0),
  //              ViewValueToDoCategory.form.fill(ToDoCategoryFormData(toDoCategory.v.name, toDoCategory.v.slug, toDoCategory.v.color))
  //            )
  //          )
  //        case None               => NotFound(views.html.error.page404())
  //      }
  //    }
  //  }
  //
  //  def update(id: Long) = Action async { implicit request: Request[AnyContent] =>
  //    ViewValueToDoCategory.form
  //      .bindFromRequest()
  //      .fold(
  //        (formWithErrors: Form[ToDoCategoryFormData]) => {
  //          Future.successful(BadRequest(views.html.category.Edit(id, formWithErrors)))
  //        },
  //        (data: ToDoCategoryFormData) => {
  //          for {
  //            oToDoCategory <- onMySQL.ToDoCategoryRepository.get(id.asInstanceOf[ToDoCategory.Id])
  //            result        <- {
  //              oToDoCategory match {
  //                case Some(toDoCategory) =>
  //                  onMySQL.ToDoCategoryRepository.update(
  //                    toDoCategory.map(
  //                      _.copy(
  //                        name  = data.name,
  //                        slug  = data.slug,
  //                        color = data.color
  //                      )
  //                    )
  //                  )
  //                case None               =>
  //                  for {
  //                    _ <- onMySQL.ToDoCategoryRepository.all()
  //                  } yield {
  //                    BadRequest(views.html.category.Edit(id, ViewValueToDoCategory.form))
  //                  }
  //              }
  //            }
  //          } yield {
  //            result match {
  //              case Some(_) => Redirect(routes.CategoryController.list())
  //              case _       => NotFound(views.html.error.page404())
  //            }
  //          }
  //        }
  //      )
  //  }
  //
  //  def delete() = Action async { implicit request: Request[AnyContent] =>
  //    val idOpt = request.body.asFormUrlEncoded.get("id").headOption
  //    idOpt match {
  //      case None     => Future.successful(NotFound(views.html.error.page404()))
  //      case Some(id) =>
  //        for {
  //          result <- onMySQL.ToDoCategoryRepository.remove(id.toLong.asInstanceOf[ToDoCategory.Id])
  //        } yield {
  //          result match {
  //            case None => NotFound(views.html.error.page404())
  //            case _    => Redirect(routes.CategoryController.list())
  //          }
//        }
//    }
//  }
}
