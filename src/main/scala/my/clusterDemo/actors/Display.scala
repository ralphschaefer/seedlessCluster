package my.clusterDemo.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

class Display(sumActorRef: ActorRef, writeToLogRef: ActorRef) extends Actor with ActorLogging {

  import my.clusterDemo.messages.DisplaySum

  override def receive: Receive = {
    case DisplaySum => {
      log.info("display sum")
      sumActorRef ! Sum.DisplayState(writeToLogRef)
    }
  }

}

object Display {

  def props(sumActorRef: ActorRef, writeToLogRef: ActorRef) = Props(new Display(sumActorRef, writeToLogRef))

}
