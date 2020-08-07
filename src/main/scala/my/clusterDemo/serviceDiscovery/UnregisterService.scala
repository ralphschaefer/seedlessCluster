package my.clusterDemo.serviceDiscovery

class UnregisterService(serviceName:String, host:String, port: Int)
  extends AbstractService[HttpBackend.Delete](serviceName, host, port)
{
  def apply(request: HttpBackend.Delete) = runService(request)
}
