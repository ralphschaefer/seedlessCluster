package my.clusterDemo.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

class Display(sumActorRef: ActorRef, writeToLogRef: ActorRef) extends Actor with ActorLogging {

  override def receive: Receive = {
    case Display.DisplaySum => {
      sumActorRef ! Sum.DisplayState(writeToLogRef)
    }
  }

}

object Display {
  trait DisplayEntity
  case object DisplaySum extends DisplayEntity

  def props(sumActorRef: ActorRef, writeToLogRef: ActorRef) = Props(new Display(sumActorRef, writeToLogRef))

}
