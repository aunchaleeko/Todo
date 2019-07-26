package com.rest.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import akka.pattern.ask
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, _}
import akka.http.scaladsl.server._
import com.rest.messages.MyTodoMsg._
import com.rest.messages._

import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import StatusCodes._
import com.rest.util.{ConvertJson, Error, TaskDescription, UpdateStatus}



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


  protected  val updateStatus: Route = {
    pathPrefix(service / "tasks" /Segment){ subject =>
      put {
        entity(as[UpdateStatus]) { param =>
          onSuccess(updateStatus(subject,param.status,param.status)) {
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



  val routes: Route = createTaskRoute ~ getTaskRoute ~ getAllTasksRoute ~ deleteTaskRoute ~ updateStatus
}

trait CoachellaApi {

  def createCoachella(): ActorRef

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  lazy val coachella: ActorRef = createCoachella()

  def createTask(subject: String, detail: String, status: String): Future[TaskResponse] = {
    println("create")
    coachella.ask(CreateTask(subject, detail,status))
      .mapTo[TaskResponse]
  }

  def getTasks(): Future[Tasks] = coachella.ask(GetTasks).mapTo[Tasks]

  def getTask(subject: String): Future[Option[Task]] = coachella.ask(GetTask(subject)).mapTo[Option[Task]]

  def deleteTask(subject: String): Future[Option[Task]] = coachella.ask(DeleteTask(subject)).mapTo[Option[Task]]



    def updateStatus2(subject: String, detail: String, status: String): Future[TaskResponse] = {
      println("update")
      coachella.ask(MyTodoMsg.UpdateStatus(subject,status)).mapTo[TaskResponse]

  }


  def updateStatus(subject: String, detail: String, status: String): Future[TaskResponse]  = {
    println("Hi")
    for {
      a <-  updateStatus2(subject,detail,status)
      b <-  createTask(subject,detail,status)
    } yield  {
      b
    }

  }



}