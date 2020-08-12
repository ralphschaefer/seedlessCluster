package my.clusterDemo.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.FromConfig

class MessageDistributor extends Actor with ActorLogging {

  val consumerRoute: ActorRef = context.system.actorOf(FromConfig.getInstance.props(), "consumerRoute")
  val displayRoute: ActorRef = context.system.actorOf(FromConfig.getInstance.props(), "display")

  override def receive: Receive = {
    case m @ Message(msg) =>
      log.info(s"distribute <$msg>")
      consumerRoute ! m
    case display: Display.DisplayEntity =>
      log.info(s"distribute <$display>")
      displayRoute ! display
  }

}

object MessageDistributor {

  def props() = Props(new MessageDistributor)
}