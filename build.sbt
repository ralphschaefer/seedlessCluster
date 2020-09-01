organization := "my.clusterDemo"

scalaVersion := "2.13.3"

version := "0.1"

name := "clusterDemo"


lazy val message = (project in file("messages")).
  settings(
    inThisBuild(List(
      scalaVersion := "2.13.3",
      organization := "my.clusterDemo",
      test in assembly := {},
      assemblyJarName in assembly := "clusterDemoMessages.jar"
    )),
    name := "clusterDemoMessages",
    assemblyJarName in assembly := name.value + ".jar",
    test in assembly := {},
    libraryDependencies ++=
      Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    )
  )

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      scalaVersion := "2.13.3",
      organization := "my.clusterDemo",
      test in assembly := {},
      assemblyJarName in assembly := "clusterDemo.jar"
    )),
    name := "clusterDemo",
    assemblyJarName in assembly := name.value + ".jar",
    test in assembly := {},
    libraryDependencies ++=
      AkkaDependencies.libs ++
      Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
      "org.json4s" %% "json4s-jackson" % "3.6.9",
      "com.twitter" %% "chill-akka" % "0.9.5"
    )
  ).dependsOn(message)

