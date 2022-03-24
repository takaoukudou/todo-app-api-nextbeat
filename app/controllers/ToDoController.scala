package controllers

import json.reads.{JsValueCreateTodo, JsValueUpdateTodo}
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

  def get(id: Long) = Action async { implicit request =>
    val toDoOptFuture = onMySQL.ToDoRepository.get(id.asInstanceOf[ToDo.Id])
    toDoOptFuture.flatMap { toDoOpt =>
      {
        toDoOpt match {
          case Some(toDo) =>
            for {
              categoryOpt <- onMySQL.ToDoCategoryRepository.get(toDo.v.categoryId)
            } yield {
              val jsValue = JsValueTodoItem(
                ViewValueToDo(
                  toDo.id,
                  toDo.v.title,
                  toDo.v.body,
                  ToDo.States(toDo.v.state).name,
                  categoryOpt.map(_.v.name).getOrElse("なし"),
                  categoryOpt.map(_.v.color).getOrElse(-1)
                )
              )
              Ok(Json.toJson(jsValue))
            }
          case _          => Future.successful(NotFound(Json.obj("message" -> "not found")))
        }
      }
    }
  }

  def store() = Action(parse.json) async { implicit request =>
    request.body
      .validate[JsValueCreateTodo]
      .fold(
        errors => {
          Future.successful(BadRequest(Json.obj("message" -> "validation error")))
        },
        todoData => {
          for {
            _ <- onMySQL.ToDoRepository
                   .add(
                     ToDo(
                       todoData.categoryId.asInstanceOf[ToDoCategory.Id],
                       todoData.title,
                       Option(todoData.body),
                       ToDo.States.TODO.code
                     )
                   )
          } yield {
            Ok(Json.obj("message" -> "store compeleted"))
          }
        }
      )
  }

  def update(id: Long) = Action(parse.json) async { implicit request =>
    request.body
      .validate[JsValueUpdateTodo]
      .fold(
        errors => {
          Future.successful(BadRequest(Json.obj("message" -> "validation error")))
        },
        todoData => {
          for {
            oToDo  <- onMySQL.ToDoRepository.get(id.asInstanceOf[ToDo.Id])
            result <- {
              oToDo match {
                case Some(toDo) =>
                  onMySQL.ToDoRepository.update(
                    toDo.map(
                      _.copy(title = todoData.title, categoryId = todoData.categoryId.asInstanceOf[ToDoCategory.Id], body = Some(todoData.body), state = todoData.state)
                    )
                  )
                case None       => Future.successful(None)
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

  def delete(id: Long) = Action async { implicit request =>
    for {
      result <- onMySQL.ToDoRepository.remove(id.asInstanceOf[ToDo.Id])
    } yield {
      result match {
        case None => NotFound(Json.obj("message" -> "not found"))
        case _    => Ok(Json.obj("message" -> "delete completed"))
      }
    }
  }
}
