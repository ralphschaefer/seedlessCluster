package my.clusterDemo

import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.Await
import my.clusterDemo.serviceDiscovery.helper.RegisterHelper
import my.clusterDemo.serviceDiscovery.MyServiceDiscovery
import scala.concurrent.duration._

object Main extends App with StrictLogging {

  println("Akka Test Node")

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
  logActor ! actors.Message("hello")

  val distribute = nodeInfo.system.actorOf(actors.MessageDistributer.props())

  println("Enter any text or 'exit'")
  var line = "N/A"
  while (line != "exit") {
    line = scala.io.StdIn.readLine()
    distribute ! actors.Message(line)
  }

  logger.debug("----- deregister self")
  Await.ready(registerHelper.deregisterSelf(), 5.seconds)

  nodeInfo.shutdown()
  MyServiceDiscovery.shutdown()

  println("stopped ...")
}
