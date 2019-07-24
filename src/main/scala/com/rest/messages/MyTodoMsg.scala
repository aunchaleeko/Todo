package com.rest.messages

import akka.actor.Props
import akka.util.Timeout
import com.rest.actors.MyTodo

object MyTodoMsg {
  def props(implicit timeout: Timeout) = Props(new MyTodo())
  case class CreateTask(subject: String, detail: String, status: String)
  case class GetTask(subject: String)
  case object GetTasks
  case class GetTickets(event: String, tickets: Int)
  case class DeleteTask(name: String)

  case class Task(name: String, detail: String, status: String)
  case class Tasks(tasks: Vector[Task])

  sealed trait TaskResponse
  case class TaskCreated(event: Task) extends TaskResponse
  case object TaskExists extends TaskResponse
}