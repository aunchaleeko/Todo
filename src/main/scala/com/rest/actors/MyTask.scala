package com.rest.actors

import akka.actor
import akka.actor.{Actor, PoisonPill}
import com.rest.messages.{MyTaskMsg, MyTodoMsg}
import com.rest.messages.MyTodoMsg.{Task, TaskCreated}

class MyTask(subject: String, detail: String, status: String) extends Actor {
  import com.rest.messages.MyTaskMsg._

  def receive: PartialFunction[Any, Unit] = {
    case  DeleteForUpdate => sender() ! TaskCreated(Task("success", "success","success"))
      println("init context.stop(self)")
      context.stop(self)
      println("finish context.stop(self)")
    case Delete => sender() ! Some(MyTodoMsg.Task(subject, detail,status))
      context.stop(self)
    case GetTask => sender() ! Some(MyTodoMsg.Task(subject, detail,status))



  }
}
