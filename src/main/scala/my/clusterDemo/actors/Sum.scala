package my.clusterDemo.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

class Sum(startValue: Int) extends Actor with ActorLogging {

  private var sum = startValue

  override def receive: Receive = {
    case Sum.Add(value) =>
      sum += value
    case Sum.DisplayState(ref) =>
      ref ! Message(s"+++++ current Sum: $sum")
  }

}

object Sum {
  case class DisplayState(writeToLog: ActorRef)
  case class Add(value: Int)
  def props(startValue: Int = 0) = Props(new Sum(startValue))
}