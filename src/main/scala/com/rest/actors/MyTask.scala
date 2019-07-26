package com.rest.actors

import akka.actor.{Actor, PoisonPill}
import com.rest.messages.MyTodoMsg
import com.rest.messages.MyTodoMsg.{Task, TaskCreated}

class MyTask(subject: String, detail: String, status: String) extends Actor {
  import com.rest.messages.MyTaskMsg._

  def receive: PartialFunction[Any, Unit] = {
    case Delete => sender() ! Some(MyTodoMsg.Task(subject, detail,status))
      self ! PoisonPill
    case GetTask => sender() ! Some(MyTodoMsg.Task(subject, detail,status))
    case  Update => sender() ! TaskCreated(Task(subject, detail,status))
      self ! PoisonPill


  }
}
//