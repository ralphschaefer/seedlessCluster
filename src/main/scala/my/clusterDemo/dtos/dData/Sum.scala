package my.clusterDemo.dtos.dData

import akka.cluster.ddata.ReplicatedData

case class Sum(result:Int) extends ReplicatedData {
  type T = Sum
  override def merge(that: Sum): Sum = {
    that // always trust other nodes :-)
  }
}
