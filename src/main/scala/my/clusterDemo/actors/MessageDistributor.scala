package my.clusterDemo.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.FromConfig

class MessageDistributor extends Actor with ActorLogging {

  import my.clusterDemo.messages.{Message,DisplaySum,DisplayEntity}

  val consumerRoute: ActorRef = context.system.actorOf(FromConfig.getInstance.props(), "consumerRoute")
  val displayRoute: ActorRef = context.system.actorOf(FromConfig.getInstance.props(), "display")

  override def receive: Receive = {
    case m @ Message(msg) =>
      log.info(s"distribute <$msg>")
      consumerRoute ! m
//  will not work with Kryo
  case display: DisplayEntity =>
    log.info(s"distribute <$display>")
    displayRoute ! display
//    case DisplaySum =>
//      log.info(s"distribute DisplaySum")
//      displayRoute ! DisplaySum
  }

}

object MessageDistributor {
  def props() = Props(new MessageDistributor)
}