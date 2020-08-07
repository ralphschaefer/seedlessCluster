package my.clusterDemo.serviceDiscovery

import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.discovery.{Lookup, ServiceDiscovery}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, _}
import scala.util.{Failure, Success, Try}

object MyServiceDiscovery {
  val actorSystemName = "ServiceDiscoverySystem"
  implicit val system = ActorSystem(actorSystemName)
  def shutdown():Unit = {
    CoordinatedShutdown.get(system).run(CoordinatedShutdown.unknownReason)
  }
}

class MyServiceDiscovery extends ServiceDiscovery with LazyLogging {

  import MyServiceDiscovery._
  import scala.concurrent.ExecutionContext.Implicits.global

  logger.info(s"create MyServiceDiscovery with ActorSystem: '$actorSystemName'")

  implicit val timeout: Timeout = 3.seconds

  lazy val port = system.settings.config.getInt("akka.discovery.my-service-discovery.port")
  lazy val host = system.settings.config.getString("akka.discovery.my-service-discovery.host")

  override def lookup(lookup: Lookup, resolveTimeout: FiniteDuration): Future[ServiceDiscovery.Resolved] = {
    logger.info(s"search for '${lookup.serviceName}''")
    val httpBackend = system.actorOf(HttpBackend.props(lookup.serviceName, host, port))

    (httpBackend ? HttpBackend.List).mapTo[Try[HttpBackend.ServiceResponse]].map {
      case Success(response) =>
         logger.info(response.result.toString())
         ServiceDiscovery.Resolved(lookup.serviceName, response.result.map(item =>
           ServiceDiscovery.ResolvedTarget(item.host, Some(item.port), None)
         ))
      case Failure(exception) =>
         logger.error(exception.getMessage)
         throw exception
    }
  }

}
