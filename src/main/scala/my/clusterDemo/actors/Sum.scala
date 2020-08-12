package my.clusterDemo.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.Cluster
import akka.cluster.ddata._
import akka.cluster.ddata.Replicator._
import my.clusterDemo.dtos.dData

import scala.concurrent.duration.DurationInt

class Sum(startValue: Int) extends Actor with ActorLogging {

  implicit val cluster = Cluster(context.system)

  val replicator: ActorRef = DistributedData(context.system).replicator

  // implicit val node = DistributedData(context.system).selfUniqueAddress
  // val DataKey = ORSetKey[String]("key")

  // replicator ! Subscribe(DataKey, self)

  val Key = ORMapKey.create[String, my.clusterDemo.dtos.dData.Sum]("sharedSum")

  replicator ! Subscribe(Key, self)

  def addToSum(value:Int) = {
    replicator ! Replicator.Update(
      key = Key,
      initial = ORMap.empty[String, dData.Sum], // dData.Sum(startValue),
      writeConsistency = Replicator.WriteMajority(5.seconds)
    ){ item: ORMap[String, dData.Sum] =>
      item + ("sharedSum" -> dData.Sum(value + item.getOrElse("sharedSum", dData.Sum(startValue)).result))
    }
  }

  // private var sum = startValue

  override def receive: Receive = {
    case Sum.Add(value) =>
      addToSum(value)
    case Sum.DisplayState(ref) =>
      replicator ! Get(Key, ReadLocal, Some(ref))
    // ref ! Message(s"+++++ current Sum: $sum")
    case g@GetSuccess(k, req) =>
       // TODO something strange here ... ???
       log.info(g.toString)
       log.info(g.get(k).toString)
       val zz = g.get(k).asInstanceOf[ORMap[String, dData.Sum]]
       zz.get("sharedSum").foreach(aa => req.get.asInstanceOf[ActorRef] ! Message(s"+++++ current Sum: ${aa.result}"))
  }

}

object Sum {
  case class DisplayState(writeToLog: ActorRef)
  case class Add(value: Int)
  def props(startValue: Int = 0) = Props(new Sum(startValue))
}
