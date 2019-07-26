package com.rest.routes

import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.util.Timeout
import akka.pattern.ask
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, _}
import akka.http.scaladsl.server._
import com.rest.messages.MyTodoMsg._
import com.rest.messages._

import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import StatusCodes._
import com.rest.util.{ConvertJson, Error, TaskDescription}



class RestApi(system: ActorSystem, timeout: Timeout) extends RestRoutes {
  implicit val requestTimeout: Timeout = timeout
  implicit def executionContext: ExecutionContextExecutor = system.dispatcher

  def createCoachella(): ActorRef = system.actorOf(MyTodoMsg.props)
}

trait RestRoutes extends CoachellaApi with ConvertJson {
  val service = "todo"

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
          onSuccess(getTasks()) { tasks =>
            complete(OK, tasks)
          }
        }
      }
    }
  }


  protected  val updateTask: Route = {
    pathPrefix(service / "tasks" /Segment){ subject =>
      put {
        entity(as[TaskDescription]) { param =>
          onSuccess(updateTask(subject,param.detail,param.status)) {
            case MyTodoMsg.TaskCreated(task) =>
              complete("success")
            case MyTodoMsg.TaskExists =>
              val err = Error(s"$subject task already exists!")
              complete(BadRequest, err)

          }
        }
      }

    }
  }


  protected  val deleteTaskRoute: Route = {
    pathPrefix(service /"delete" /Segment){ subject =>
      delete{
        pathEndOrSingleSlash{
          onSuccess(deleteTask(subject)) {
            _.fold(complete(NotFound))(e => complete(OK,e))

          }
        }
      }
    }
  }

  val routes: Route = createTaskRoute ~ getTaskRoute ~ getAllTasksRoute ~ deleteTaskRoute ~ updateTask
}

trait CoachellaApi {

  def createCoachella(): ActorRef

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  lazy val coachella: ActorRef = createCoachella()

  def createTask(subject: String, detail: String, status: String): Future[TaskResponse] = {
    coachella.ask(CreateTask(subject, detail,status))
      .mapTo[TaskResponse]
  }

  def createForUpdateTask(subject: String, detail: String, status: String): Future[TaskResponse] = {
    println("init create")
    coachella.ask(CFUpdateTask(subject, detail,status))
      .mapTo[TaskResponse]
  }
  def getTasks(): Future[Tasks] = coachella.ask(GetTasks).mapTo[Tasks]

  def getTask(subject: String): Future[Option[Task]] = coachella.ask(GetTask(subject)).mapTo[Option[Task]]

  def deleteTask(subject: String): Future[Option[Task]] = coachella.ask(DeleteTask(subject)).mapTo[Option[Task]]



    def deleteforUpdate(subject: String, detail: String, status: String): Future[TaskResponse] = {
      coachella.ask(MyTodoMsg.DeleteForUpdateTask(subject,status)).mapTo[TaskResponse]

  }


  def updateTask(subject: String, detail: String, status: String): Future[TaskResponse]  = {
    for {
      d <-  deleteforUpdate(subject,detail,status)
      c <-  createForUpdateTask(subject,detail,status)
    } yield  {
      println(List(c))
      createForUpdateTask(subject,detail,status)
      d
    }

  }


}