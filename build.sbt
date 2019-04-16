name := "Destiny"

version := "1.0"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-streaming" % "2.4.0" % "provided",
  "org.apache.spark" %% "spark-sql" % "2.4.0",
  "org.apache.spark" %% "spark-mllib" % "2.4.0",
  "org.apache.spark" %% "spark-core" % "2.4.0",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
  "org.ccil.cowan.tagsoup" % "tagsoup" % "1.2.1",
  "net.sourceforge.htmlcleaner"%"htmlcleaner"%"2.6.1"
)