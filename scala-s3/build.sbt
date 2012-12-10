name := "scala-s3"

version := "1.0.0"

organization := "com.codeminders"

scalaVersion := "2.9.2"

libraryDependencies := Seq(
	"org.apache.httpcomponents" % "httpclient" % "4.2.1",	
	"org.scalatest" % "scalatest_2.9.2" % "1.8",
	"junit" % "junit" % "4.10",
	"commons-io" % "commons-io" % "2.4",
	"commons-codec" % "commons-codec" % "1.7"
	)
