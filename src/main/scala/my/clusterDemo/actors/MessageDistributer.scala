package my.clusterDemo.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.FromConfig

class MessageDistributer extends Actor with ActorLogging {

  val consumerRoute: ActorRef = context.system.actorOf(FromConfig.getInstance.props(), "consumerRoute")

  override def receive: Receive = {
    case m @ Message(msg) =>
      log.info(s"distribute <$msg>")
      consumerRoute ! m
  }

}

object MessageDistributer {

  def props() = Props(new MessageDistributer)
}