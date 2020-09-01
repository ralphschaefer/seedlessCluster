package my.clusterDemo.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._

class WriteToLog extends Actor with ActorLogging {

  import my.clusterDemo.messages.Message

  val cluster = Cluster(context.system)

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberEvent])

  def logMemberEvents(m:MemberEvent) = log.info(s"----> ${m}")

  override def receive: Receive = {

    case m @ MemberUp(member) =>
      logMemberEvents(m)
    case m @ MemberJoined(member) =>
      logMemberEvents(m)
    case m @ MemberExited(member) =>
      logMemberEvents(m)
    case m @ MemberRemoved(member, previousStatus) =>
      logMemberEvents(m)
    case Message(msg) =>
      log.info(s"----> from :${sender.path.toStringWithoutAddress} -> $msg")
    case other =>
      log.error(s"unknown message: ${other.toString}")
  }

}

object WriteToLog {
  def props() = Props(new WriteToLog)
}
