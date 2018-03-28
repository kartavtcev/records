name := "backdating-records"

mainClass in Compile := Some("RecordsApp")

libraryDependencies ++= List(
  "com.typesafe.slick" %% "slick" % "3.1.1",
  "com.h2database" % "h2" % "1.4.187",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

fork in run := true
