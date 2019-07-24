package com.rest.actors

import akka.actor.{Actor, PoisonPill}
import com.rest.messages.MyTodoMsg

class MyTask(subject: String, detail: String, status: String) extends Actor {
  import com.rest.messages.MyTaskMsg._

  def receive: PartialFunction[Any, Unit] = {

    case GetTask ⇒ sender() ! Some(MyTodoMsg.Task(subject, detail,status))
    case Delete ⇒ sender() ! Some(MyTodoMsg.Task(subject, detail,status))
      self ! PoisonPill
  }
}
