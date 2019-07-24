package com.rest.actors

import akka.actor._
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.rest.messages.MyTodoMsg._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class MyTodo(implicit timeout: Timeout) extends Actor {
  import com.rest.messages.MyTaskMsg


  def createTaskTodo(subject: String, detail: String, status: String): ActorRef = {
    context.actorOf(MyTaskMsg.props(subject,detail,status), subject)
  }

  def receive: PartialFunction[Any, Unit] = {
    case CreateTask(subject, deatail,status) ⇒
      def create(): Unit = {
        //        creates the task
       createTaskTodo(subject,deatail,status)
        sender() ! TaskCreated(Task(subject, deatail,status))
      }
      //      If task exists it responds with task Exists
      context.child(subject).fold(create())(_ ⇒ sender() ! TaskExists)




    case GetTask(subject) =>
      def notFound() = sender() ! None
      def getTask(child: ActorRef) = child forward MyTaskMsg.GetTask
      context.child(subject).fold(notFound())(getTask)


    case GetTasks ⇒
      def getTasks = {
        context.children.map { child ⇒
          //          asks all task
          self.ask(GetTask(child.path.name)).mapTo[Option[Task]]
        }
      }
      def convertToTask(f: Future[Iterable[Option[Task]]]): Future[Tasks] = {
        f.map(_.flatten).map(l ⇒ Tasks(l.toVector))
      }
      pipe(convertToTask(Future.sequence(getTasks))) to sender()


    case DeleteTask(subject) ⇒
      def notFound(): Unit = sender() ! None
      //      ActorRef carries the message that should be sent to an Actor
      //      forward the message  that an task was delete
      def deleteTask(child: ActorRef): Unit = child forward MyTaskMsg.Delete
      context.child(subject).fold(notFound())(deleteTask)
  }

}