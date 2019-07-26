package com.rest.messages


import akka.actor.Props
import com.rest.actors.MyTask


object MyTaskMsg {

  def props (subject: String,detail: String,status: String) = Props(new MyTask (subject,detail,status))
  case object GetTask
  case object Delete
  case object DeleteForUpdate
  case object CreateForUpdate

}