package my.clusterDemo.serviceDiscovery.helper

import akka.actor.ActorSystem
import com.typesafe.scalalogging.StrictLogging
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._

import my.clusterDemo.serviceDiscovery.{RegisterService, UnregisterService}
import my.clusterDemo.serviceDiscovery.HttpBackend.{Create, Delete}
import my.clusterDemo.serviceDiscovery.HttpBackend

object RegisterHelper {
  val configName = "my-service-discovery"
}

class RegisterHelper(sys: ActorSystem, systemName:String, instance:Int) extends StrictLogging {

  import RegisterHelper._

  private var id:Option[String] = None

  def getRegisterId: Option[String] = id

  def registerSelf(): Future[Try[HttpBackend.ServiceResponse]] = {
    lazy val port = sys.settings.config.getInt(s"akka.discovery.$configName.port")
    lazy val host = sys.settings.config.getString(s"akka.discovery.$configName.host")
    lazy val serviceName = sys.settings.config.getString("akka.management.cluster.bootstrap.contact-point-discovery.effective-name")
    val register = new RegisterService(serviceName, host, port)
    register(Create(
      sys.settings.config.getString("akka.management.http.hostname"),
      sys.settings.config.getInt("akka.management.http.port"),
      systemName,
      instance
    ))
  }

  def waitForRegisterId(): Option[String] = {
    id = Await.ready(registerSelf(), 5.seconds).value.map(_.flatMap {
      case Success(serviceResponse) =>
        val createdService: HttpBackend.Service = serviceResponse.result.headOption.get
        val res: String = createdService.id.getOrElse {
          logger.error("missing Service id")
          System.exit(1)
          ""
        }
        Success(res)
      case Failure(exception) =>
        logger.error(s"error :$exception")
        System.exit(1)
        Success("")
    }).map(_.get)
    id
  }

  def deregisterSelf(): Future[Try[HttpBackend.ServiceResponse]] = {
    if (id.isEmpty ){
      Future.failed(throw new Exception("missing id"))
    } else {
      lazy val port = sys.settings.config.getInt(s"akka.discovery.$configName.port")
      lazy val host = sys.settings.config.getString(s"akka.discovery.$configName.host")
      lazy val serviceName = sys.settings.config.getString("akka.management.cluster.bootstrap.contact-point-discovery.effective-name")
      val deregister = new UnregisterService(serviceName, host, port)
      deregister(Delete(id.get))
    }
  }

}
