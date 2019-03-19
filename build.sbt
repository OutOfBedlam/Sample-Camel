name := "Sample-Camel"

version := "0.1"

scalaVersion := "2.12.8"

val camelVersion = "2.21.5"

libraryDependencies ++= Seq(
  "org.apache.camel" % "camel-core",
  "org.apache.camel" % "camel-sql",
  "org.apache.camel" % "camel-spark-rest",
  "org.apache.camel" % "camel-swagger-java",
  "org.apache.camel" % "camel-jackson",
  "org.apache.camel" % "camel-http4",
).map( _ % camelVersion)

// logging libraries
libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)

// H2 Database
libraryDependencies += "com.h2database" % "h2" % "1.4.198"

// libraries for scala-test and camel-test
libraryDependencies ++= Seq(
  "org.apache.camel" % "camel-test" % camelVersion,
).map( _ % Test)












