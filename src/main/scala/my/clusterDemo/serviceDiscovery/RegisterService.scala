package my.clusterDemo.serviceDiscovery

class RegisterService(serviceName:String, host:String, port: Int)
  extends AbstractService[HttpBackend.Create](serviceName, host, port)
{
  def apply(request: HttpBackend.Create) = runService(request)
}
