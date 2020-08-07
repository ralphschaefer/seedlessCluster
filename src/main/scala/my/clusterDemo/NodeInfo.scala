package my.clusterDemo

import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.cluster.Cluster
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.typesafe.config.Config

trait NodeInfo {
  val system: ActorSystem
  val config: Config
  val httpClusterManagement: AkkaManagement
  val clusterBootstrap: ClusterBootstrap
  val cluster: Cluster

  def shutdown():Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    httpClusterManagement.stop().onComplete (
      _ => println("cluster Management stopped")
    )
    CoordinatedShutdown.get(system).run(CoordinatedShutdown.unknownReason)
  }

}

object NodeInfo {

  def apply(actorSystemName: String) = new NodeInfo {
    val system: ActorSystem = ActorSystem(actorSystemName)
    lazy val config: Config = system.settings.config
    lazy val httpClusterManagement = AkkaManagement(system)
    lazy val clusterBootstrap = ClusterBootstrap(system)
    lazy val cluster: Cluster = Cluster(system)

    def startClusterBootstrap():Unit =
    {
      httpClusterManagement.start().onComplete( _ =>
        println("start mngmt")
      )(system.dispatcher)
      clusterBootstrap.start()
    }
  }

}
