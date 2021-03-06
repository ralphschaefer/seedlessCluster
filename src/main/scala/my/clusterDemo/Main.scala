package my.clusterDemo

import java.lang.reflect.Field

import akka.actor.ActorRef
import akka.routing.FromConfig
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.Await
import my.clusterDemo.serviceDiscovery.helper.RegisterHelper
import my.clusterDemo.serviceDiscovery.MyServiceDiscovery
import my.clusterDemp.KRWarning

import scala.concurrent.duration._


object Main extends App with StrictLogging {

  import my.clusterDemo.messages.DisplaySum
  import my.clusterDemo.messages.Add
  import my.clusterDemo.messages.Message

  println("Akka Test Node")
  KRWarning.disable()

  logger.debug("---- connect to cluster")
  val nodeInfo = NodeInfo("miniNode") // TODO get from config
  val registerHelper = new RegisterHelper(
    nodeInfo.system,
    nodeInfo.system.settings.config.getString("settings.actorSystemName"),
    nodeInfo.system.settings.config.getInt("settings.instance")
  )
  nodeInfo.startClusterBootstrap()

  logger.debug("----- register self")
  val regId = registerHelper.waitForRegisterId().getOrElse {
    logger.error("Timeout ...")
    System.exit(1)
    ""
  }
  logger.info(s"----- registered id '$regId'")

  logger.debug("---- bring up writelog actor")
  val logActor = nodeInfo.system.actorOf(actors.WriteToLog.props(), "logEndpoint")
  logActor ! Message("hello")

  logger.debug("---- bring up sum actor")
  val sumActor = nodeInfo.system.actorOf(actors.Sum.props(0), "sumData")

  logger.debug("---- bing up display actor")
  val displayActor = nodeInfo.system.actorOf(actors.Display.props(sumActor, logActor), "displayData")

  val distribute = nodeInfo.system.actorOf(actors.MessageDistributor.props(), "distributor")

  println("Enter any text or 'exit'")
  var line = "N/A"
  val sumMatcher = """^sum\(([0-9]+)\)$""".r
  val sumRoute: ActorRef = nodeInfo.system.actorOf(FromConfig.getInstance.props(), "sumRoute")

  while (line != "exit") {
    line = scala.io.StdIn.readLine()
    line match {
      case "display" =>
        distribute ! DisplaySum
      case sumMatcher(s) =>
        sumRoute ! Add(s.toInt)
      case other =>
        distribute ! Message(other)
    }
  }

  logger.debug("----- deregister self")
  Await.ready(registerHelper.deregisterSelf(), 5.seconds)

  nodeInfo.shutdown()
  MyServiceDiscovery.shutdown()

  println("stopped ...")
}
