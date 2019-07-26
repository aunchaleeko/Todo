package com.rest.util

import play.api.libs.json._
import com.rest.messages.MyTodoMsg._
import de.heikoseeberger.akkahttpplayjson._

// message containing the initial number of tickets for the event
case class TaskDescription(detail: String,status: String) {
  require(detail != "")
  require(status != "")
}
case class UpdateStatus(status: String){
  require(status != "")
}

// message containing an error
case class Error(message: String)

// convert our case classes from and to JSON
trait ConvertJson extends PlayJsonSupport {

  implicit val taskDescriptionFormat: OFormat[TaskDescription] = Json.format[TaskDescription]
  implicit val updateDescriptionFormat: OFormat[UpdateStatus] = Json.format[UpdateStatus]
  implicit val errorFormat: OFormat[Error] = Json.format[Error]
  implicit val taskFormat: OFormat[Task] = Json.format[Task]
  implicit val tasksFormat: OFormat[Tasks] = Json.format[Tasks]
}

object ConvertJson extends ConvertJson