name := "Sample-Camel"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "org.apache.camel" % "camel-core",
  "org.apache.camel" %"camel-sql"
).map( _ % "2.21.5")

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.0.13"
)

libraryDependencies += "com.h2database" % "h2" % "1.4.198"
