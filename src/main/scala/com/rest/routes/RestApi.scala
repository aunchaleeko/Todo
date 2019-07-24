package com.rest.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import akka.pattern.ask
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.rest.messages.MyTodoMsg._
import com.rest.messages._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import StatusCodes._
import com.rest.util.{Error, ConvertJson, TaskDescription}


class RestApi(system: ActorSystem, timeout: Timeout) extends RestRoutes {
  implicit val requestTimeout: Timeout = timeout
  implicit def executionContext: ExecutionContextExecutor = system.dispatcher

  def createCoachella(): ActorRef = system.actorOf(MyTodoMsg.props)
}

trait RestRoutes extends CoachellaApi with ConvertJson {
  val service = "my-todo"

  protected val createTaskRoute: Route = {
    pathPrefix(service  / "tasks" / Segment ) { subject ⇒
      post {
        entity(as[TaskDescription]) { td =>
          onSuccess(createTask(subject,td.detail,td.status)) {
            case MyTodoMsg.TaskCreated(task) => complete(Created,task)
            case MyTodoMsg.TaskExists =>
              val err = Error(s"$subject task already exists!")
              complete(BadRequest, err)
          }
        }
      }
    }
  }

  protected val getTaskRoute: Route = {
    pathPrefix(service  / "tasks" / Segment) { subject ⇒
      get {
        pathEndOrSingleSlash {
          onSuccess(getTask(subject)) {
            _.fold(complete(NotFound))(e ⇒ complete(OK, e))
          }
        }
      }
      
    }
  }

  protected val getAllTasksRoute: Route = {
    pathPrefix(service / "tasks") {
      get {
        pathEndOrSingleSlash {
          onSuccess(getTasks()) { tasks ⇒
            complete(OK, tasks)
          }
        }
      }
    }
  }

  protected  val deleteTaskRoute: Route = {
    pathPrefix(service /"tasks" /Segment){ subject =>
      delete{
        pathEndOrSingleSlash{
          onSuccess(deleteTask(subject)) {
            _.fold(complete(NotFound))(e => complete(OK,e))

          }
        }
      }
    }
  }




  val routes: Route = createTaskRoute ~ getTaskRoute ~ getAllTasksRoute ~ deleteTaskRoute
}

trait CoachellaApi {

  def createCoachella(): ActorRef

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  lazy val coachella: ActorRef = createCoachella()

  def createTask(event: String, detail: String, flag: String): Future[TaskResponse] = {
    coachella.ask(CreateTask(event, detail,flag))
      .mapTo[TaskResponse]
  }

  def getTasks(): Future[Tasks] = coachella.ask(GetTasks).mapTo[Tasks]

  def getTask(subject: String): Future[Option[Task]] = coachella.ask(GetTask(subject)).mapTo[Option[Task]]

  def deleteTask(subject: String): Future[Option[Task]] = coachella.ask(DeleteTask(subject)).mapTo[Option[Task]]

}