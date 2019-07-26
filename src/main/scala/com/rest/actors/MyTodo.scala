package com.rest.actors

import akka.actor
import akka.actor._
import akka.event.Logging
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.rest.ServiceMain.system
import com.rest.messages.MyTodoMsg._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class MyTodo(implicit timeout: Timeout) extends Actor {
  import com.rest.messages.MyTaskMsg
  val log = Logging(system.eventStream, "todo")


  def receive: PartialFunction[Any, Unit] = {
    case CreateTask(subject, detail,status) =>

      def create(): Unit = {
        //        creates the task
        println("receive create1")
        context.actorOf(MyTaskMsg.props(subject,detail,status), subject)
        println("receive create2")
        sender() ! TaskCreated(Task(subject, detail,status))
        println("receive create3")
      }
      //      If task exists it responds with task Exists
      println("receive create")
      context.child(subject).fold(create())(_ => sender() ! TaskExists)


    case GetTask(subject) =>
      def notFound() : Unit = sender() ! None
      def getTask(child: ActorRef) : Unit = child forward MyTaskMsg.GetTask
      context.child(subject).fold(notFound())(getTask)

    case GetTasks ⇒
      def getTasks = {
        context.children.map { child =>
          //          asks all task
          self.ask(GetTask(child.path.name)).mapTo[Option[Task]]
        }
      }
      def convertToTask(f: Future[Iterable[Option[Task]]]): Future[Tasks] = {
        f.map(_.flatten).map(l => Tasks(l.toVector))
      }
      pipe(convertToTask(Future.sequence(getTasks))) to sender()


    case DeleteTask(subject) =>
      def notFound(): Unit = sender() ! None
      //      ActorRef carries the message that should be sent to an Actor
      //      forward the message  that an task was delete
      def deleteTask(child: ActorRef): Unit = child forward MyTaskMsg.Delete
      context.child(subject).fold(notFound())(deleteTask)


    case UpdateStatus(subject,status) =>
      def notFound(): Unit = sender() ! None
      def deleteTask(child: ActorRef): Unit = child forward MyTaskMsg.Update
      context.child(subject).fold(notFound())(deleteTask)




  }



}