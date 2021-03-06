package my.clusterDemo.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.Cluster
import akka.cluster.ddata._
import akka.cluster.ddata.Replicator._
import my.clusterDemo.dtos.dData

import scala.concurrent.duration.DurationInt

// [scaladays talk distributed data](https://www.youtube.com/watch?v=NQKxDxn5olM)
// [CRDT(heise)](https://m.heise.de/developer/artikel/Verteilte-Daten-ohne-Muehe-Conflict-Free-Replicated-Data-Types-3944421.html?seite=all)

class Sum(startValue: Int) extends Actor with ActorLogging {

  import my.clusterDemo.messages.{Add, Message}

  implicit val cluster = Cluster(context.system)

  val replicator: ActorRef = DistributedData(context.system).replicator

  val mykey = new Key[my.clusterDemo.dtos.dData.Sum]("sharedSum") with ReplicatedDataSerialization

  replicator ! Subscribe(mykey, self)

  def addToSum(value:Int) = {
    replicator ! Replicator.Update(
      key = mykey,
      initial = dData.Sum(startValue),
      writeConsistency = Replicator.WriteMajority(5.seconds)
    ){
      item: dData.Sum => dData.Sum(value + item.result)
    }
  }

  override def receive: Receive = {
    case Add(value) =>
      log.info(s"---- add $value")
      addToSum(value)
    case Sum.DisplayState(ref) =>
      log.info("do display")
      replicator ! Get(mykey, ReadLocal, Some(ref))
    case g@GetSuccess(k, req) =>
       val theSum:dData.Sum  = g.get(k).asInstanceOf[dData.Sum]
       log.info("display " + theSum.result)
       req.get.asInstanceOf[ActorRef] ! Message(s"+++++ current Sum: ${theSum.result}")
    case other =>
       log.info("other Messages: " + other.toString)
  }

}

object Sum {
  case class DisplayState(writeToLog: ActorRef)
  def props(startValue: Int = 0) = Props(new Sum(startValue))
}
