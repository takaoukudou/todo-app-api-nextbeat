package model

import lib.model.ToDo

case class ViewValueToDo(
    id:          ToDo.Id,
    title:       String,
    body:        Option[String],
    stateStr:    String,
    categoryStr: String,
    color:       Short
)
