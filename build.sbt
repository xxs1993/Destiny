name := "Destiny"

version := "1.0"

scalaVersion := "2.11.9"
val akkaGroup = "com.typesafe.akka"
val akkaVersion = "2.4.1"
libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
  "org.ccil.cowan.tagsoup" % "tagsoup" % "1.2.1",
  "net.sourceforge.htmlcleaner"%"htmlcleaner"%"2.6.1",
  akkaGroup %% "akka-actor" % akkaVersion,
  akkaGroup %% "akka-testkit" % akkaVersion % "test",
  akkaGroup %% "akka-slf4j" % akkaVersion,
  "com.typesafe" % "config" % "1.3.0",
  "ch.qos.logback" % "logback-classic" % "1.0.0" % "runtime"
)

val scalaTestVersion = "2.2.4"

libraryDependencies += "org.scalatest" %% "scalatest" % scalaTestVersion % "test"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.0"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.0"

parallelExecution in Test := false

libraryDependencies += "io.spray" %% "spray-json" % "1.3.4"