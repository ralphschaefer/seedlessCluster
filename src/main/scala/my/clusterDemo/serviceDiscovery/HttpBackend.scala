package my.clusterDemo.serviceDiscovery

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.util.ByteString
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class HttpBackend(className: String, host: String, port: Int) extends Actor with ActorLogging {

  protected def uri(id: Option[String] = None): String = {
    s"http://$host:$port/service/$className" + id.map(i => s"/$i").getOrElse("")
  }

  import akka.pattern.pipe

  implicit val formats = org.json4s.DefaultFormats

  implicit val ec: ExecutionContext = context.dispatcher

  implicit val actorSystem = context.system

  val http = Http(context.system)

  override def receive: Receive = {

    case del:HttpBackend.Delete =>
      log.info(s"Delete Node info '${del.id}'")
      val theSender = sender()
      http.singleRequest(
        HttpRequest(
          method = HttpMethods.DELETE,
          uri(Some(del.id)),
          entity = HttpEntity.empty(ContentTypes.`application/json`)
        )
      ).map{response =>
        log.info(response.toString())
        HttpBackend.httpWithReplyActor(theSender, response)
      }.pipeTo(self)

    case cr:HttpBackend.Create =>
      log.info(s"Create Node info '${cr.asDto}'")
      val theSender = sender()
      http.singleRequest(
        HttpRequest(
          method = HttpMethods.POST,
          uri(),
          entity = HttpEntity(ContentTypes.`application/json`, write(cr.asDto))
        )
      ).map{response =>
        log.info(response.toString())
        HttpBackend.httpWithReplyActor(theSender, response)
      }.pipeTo(self)

    case HttpBackend.List =>
      log.info("Query nodes")
      val theSender = sender()
      log.info(sender().toString())
      log.info(s"querying: ${uri()}")
      http.singleRequest(
        HttpRequest(
          method = HttpMethods.GET,
          uri(),
          entity = HttpEntity.empty(ContentTypes.`application/json`)
        )
      ).map{response =>
        log.info(response.toString())
        HttpBackend.httpWithReplyActor(theSender, response)
      }.pipeTo(self)

    case HttpBackend.httpWithReplyActor(replyTo, HttpResponse(StatusCodes.OK, _, entity, _)) =>
      log.info("got Http response")
      val response = entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(bytes =>
        parse(bytes.utf8String).extract[HttpBackend.ServiceResponse]
      )
      response.andThen {
        case f @ Failure(t) =>
          log.error("Error " + t)
          replyTo ! f
        case s @ Success(v) =>
          log.info(v.toString)
          log.info(replyTo.toString())
          replyTo ! s
      }

    case HttpBackend.httpWithReplyActor(replyTo, HttpResponse(code, _, _, _)) =>
      log.error(s"ERROR from server : $code")
      replyTo ! Failure(HttpBackend.ResultHttpError(code))

    case anyMessage =>
      log.error("unknown Message")
      log.error(anyMessage.toString)
  }

}

object HttpBackend {

  def props(className:String, host:String, port:Int) = Props(new HttpBackend(className, host, port))

  case class httpWithReplyActor(replyTo: ActorRef, httpResponse: HttpResponse)

  trait ServiceRequest

  case object List extends ServiceRequest
  case class Create(host: String, port: Int, name: String, instance: Int) extends ServiceRequest {
    def asDto = my.clusterDemo.dtos.serviceDiscovery.Register(host,port,name,instance)
  }
  case class Delete(id: String) extends ServiceRequest
  case class Query(Id: String) extends ServiceRequest
  case class ResultHttpError(code: StatusCode) extends Exception


  case class Service(id: Option[String], host: String, `class`: Option[String], port: Int, name: String, instance: String)
  case class ServiceResponse(action: String, result: List[Service])


}