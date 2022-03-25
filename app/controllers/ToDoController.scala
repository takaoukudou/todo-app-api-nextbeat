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
    for {
      toDoOpt     <- onMySQL.ToDoRepository.get(id.asInstanceOf[ToDo.Id])
      categoryOpt <- toDoOpt.fold(Future.successful(Option.empty[ToDoCategory.EmbeddedId])) { toDo =>
                       onMySQL.ToDoCategoryRepository.get(toDo.v.categoryId)
                     }
    } yield {
      toDoOpt.fold(NotFound(Json.obj("message" -> "not found"))) { toDo =>
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
          val toDoCategory = onMySQL.ToDoCategoryRepository.get(todoData.categoryId.asInstanceOf[ToDoCategory.Id])
          for {
            id           <- onMySQL.ToDoRepository
                              .add(
                                ToDo(
                                  todoData.categoryId.asInstanceOf[ToDoCategory.Id],
                                  todoData.title,
                                  Option(todoData.body),
                                  ToDo.States.TODO.code
                                )
                              )
            toDoCategory <- toDoCategory
          } yield {
            Ok(
              Json.toJson(
                JsValueTodoItem(
                  ViewValueToDo(
                    id,
                    todoData.title,
                    Option(todoData.body),
                    ToDo.States.TODO.name,
                    toDoCategory.map(_.v.name).getOrElse("なし"),
                    toDoCategory.map(_.v.color).getOrElse(-1)
                  )
                )
              )
            )
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
            oToDo        <- onMySQL.ToDoRepository.get(id.asInstanceOf[ToDo.Id])
            updateOTodo  <- oToDo.fold(Future.successful(Option.empty[ToDo.EmbeddedId])) { toDo =>
                              onMySQL.ToDoRepository.update(
                                toDo.map(
                                  _.copy(title = todoData.title, categoryId = todoData.categoryId.asInstanceOf[ToDoCategory.Id], body = Some(todoData.body), state = todoData.state)
                                )
                              )
                            }
            toDoCategory <- updateOTodo.fold(Future.successful(Option.empty[ToDoCategory.EmbeddedId])) { _ =>
                              onMySQL.ToDoCategoryRepository.get(todoData.categoryId.asInstanceOf[ToDoCategory.Id])
                            }
          } yield updateOTodo match {
            case None    => NotFound(Json.obj("message" -> "not found"))
            case Some(_) =>
              Ok(
                Json.toJson(
                  JsValueTodoItem(
                    ViewValueToDo(
                      id.asInstanceOf[ToDo.Id],
                      todoData.title,
                      Some(todoData.body),
                      ToDo.States(todoData.state).name,
                      toDoCategory.map(_.v.name).getOrElse("なし"),
                      toDoCategory.map(_.v.color).getOrElse(-1)
                    )
                  )
                )
              )
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
        case _    => Ok(Json.obj("id" -> id))
      }
    }
  }
}
