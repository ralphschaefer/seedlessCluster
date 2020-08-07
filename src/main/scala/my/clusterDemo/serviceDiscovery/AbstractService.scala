package my.clusterDemo.serviceDiscovery

import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._

abstract class AbstractService[A <: HttpBackend.ServiceRequest](serviceName:String, host:String, port: Int) extends LazyLogging {

  import MyServiceDiscovery._
  import akka.pattern.ask
  implicit val timeout: Timeout = 3.seconds
  import scala.concurrent.ExecutionContext.Implicits.global

  protected def runService(request: A) = {
    val httpBackend = system.actorOf(HttpBackend.props(serviceName, host, port))
    (httpBackend ? request).mapTo[Try[HttpBackend.ServiceResponse]].map{
      case s @ Success(response) =>
         logger.info(response.result.toString())
         s
      case f @ Failure(exception) =>
         logger.error(exception.getMessage)
         f
    }
  }

  def apply(request: A):Future[Try[HttpBackend.ServiceResponse]]

}
