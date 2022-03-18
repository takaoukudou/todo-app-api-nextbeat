package controllers

import json.reads.JsValueCreateTodo
import json.writes.JsValueTodoItem
import lib.model.{ToDo, ToDoCategory}
import lib.persistence.onMySQL
import model.ViewValueToDo
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ToDoController @Inject() (val controllerComponents: ControllerComponents)(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  def list(): Action[AnyContent] = Action async { implicit request: Request[AnyContent] =>
    val toDos = onMySQL.ToDoRepository.all()
    for {
      toDoCategories <- onMySQL.ToDoCategoryRepository.all()
      toDos          <- toDos
    } yield {
      val jsValueTodoItemList = toDos.map(toDo => {
        val categoryOpt = toDoCategories.find(_.id == toDo.v.categoryId).map(_.v)
        JsValueTodoItem(
          ViewValueToDo(
            toDo.id,
            toDo.v.title,
            toDo.v.body,
            ToDo.States(toDo.v.state).name,
            categoryOpt.map(_.name).getOrElse("なし"),
            categoryOpt.map(_.color).getOrElse(-1)
          )
        )
      })
      Ok(Json.toJson(jsValueTodoItemList))
    }
  }

  //
  //  private def getViewValueToDoCategories(toDoCategories: Seq[ToDoCategory.EmbeddedId]) = {
  //    toDoCategories.map(toDoCategory => ViewValueToDoCategory(toDoCategory.id, toDoCategory.v.name, toDoCategory.v.slug, toDoCategory.v.color))
  //  }
  //
  //  def register() = Action async { implicit request: Request[AnyContent] =>
  //    for {
  //      toDoCategories <- onMySQL.ToDoCategoryRepository.all()
  //    } yield {
  //      Ok(views.html.todo.Store(getViewValueToDoCategories(toDoCategories), ViewValueToDo.form))
  //    }
  //  }
  //
  def store() = Action(parse.json) async { implicit request =>
    request.body
      .validate[JsValueCreateTodo]
      .fold(
        errors => {
          println(errors)
          Future.successful(Ok("error"))
        },
        todoData => {
          for {
            _ <- onMySQL.ToDoRepository
                   .add(
                     ToDo(
                       todoData.categoryId.toLong.asInstanceOf[ToDoCategory.Id],
                       todoData.title,
                       Option(todoData.body),
                       ToDo.States.TODO.code
                     )
                   )
          } yield {
            Ok("store complete")
          }
        }
      )
  }
  //
  //  def edit(id: Long) = Action async { implicit request: Request[AnyContent] =>
  //    val toDoCategories = onMySQL.ToDoCategoryRepository.all()
  //    for {
  //      toDo           <- onMySQL.ToDoRepository.get(id.asInstanceOf[ToDo.Id])
  //      toDoCategories <- toDoCategories
  //    } yield {
  //      toDo match {
  //        case Some(toDo) =>
  //          Ok(
  //            views.html.todo.Edit(
  //              toDo.v.id.getOrElse(0),
  //              getViewValueToDoCategories(toDoCategories),
  //              ViewValueToDo.form
  //            )
  //          )
  //        case None       => NotFound(views.html.error.page404())
  //      }
  //    }
  //  }
  //
  //  def update(id: Long) = Action async { implicit request: Request[AnyContent] =>
  //    ViewValueToDo.form
  //      .bindFromRequest()
  //      .fold(
  //        (formWithErrors: Form[ToDoFormData]) => {
  //          for {
  //            toDoCategories <- onMySQL.ToDoCategoryRepository.all()
  //          } yield {
  //            BadRequest(views.html.todo.Edit(id, getViewValueToDoCategories(toDoCategories), formWithErrors))
  //          }
  //        },
  //        (data: ToDoFormData) => {
  //          for {
  //            oToDo  <- onMySQL.ToDoRepository.get(id.asInstanceOf[ToDo.Id])
  //            result <- {
  //              oToDo match {
  //                case Some(toDo) =>
  //                  onMySQL.ToDoRepository.update(
  //                    toDo.map(
  //                      _.copy(
  //                        title      = data.title,
  //                        categoryId = data.categoryId,
  //                        body       = Some(data.body),
  //                        state      = data.state.get.toShort
  //                      )
  //                    )
  //                  )
  //                case None       =>
  //                  for {
  //                    toDoCategories <- onMySQL.ToDoCategoryRepository.all()
  //                  } yield {
  //                    BadRequest(views.html.todo.Edit(id, getViewValueToDoCategories(toDoCategories), ViewValueToDo.form))
  //                  }
  //              }
  //            }
  //          } yield {
  //            result match {
  //              case Some(_) => Redirect(routes.ToDoController.list())
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
  //          result <- onMySQL.ToDoRepository.remove(id.toLong.asInstanceOf[ToDo.Id])
  //        } yield {
  //          result match {
  //            case None => NotFound(views.html.error.page404())
  //            case _    => Redirect(routes.ToDoController.list())
  //          }
  //        }
  //    }
  //  }
}
