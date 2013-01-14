import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object ScalaAWSBuild extends Build {

  import Dependencies._

  lazy val buildSettings = Seq(
    organization := "com.codeminders.scalaws",
    version      := "1.0.0",
    scalaVersion := "2.9.2")

  lazy val root = Project(
    id = "scala-aws",
    base = file("."),
    settings = Defaults.defaultSettings) aggregate (core, s3, examples)

  lazy val core = Project(
    id = "scala-aws-core",
    base = file("scala-aws-core"), 
    settings = Defaults.defaultSettings ++ buildSettings ++ Seq(libraryDependencies ++= Seq(Compile.httpclient, Compile.commonsio, Compile.commonscodec, Test.junit, Test.scalatest)))

  lazy val s3 = Project(
    id = "scala-aws-s3",
    base = file("scala-aws-s3"),
    settings = Defaults.defaultSettings ++ buildSettings ++ Seq(libraryDependencies ++= Seq(Compile.commonsio, Compile.commonscodec, Test.junit, Test.scalatest))) dependsOn (core)

  lazy val examples = Project(id = "scala-aws-examples",
    base = file("scala-aws-examples"), settings = Defaults.defaultSettings ++ buildSettings ++ Seq(libraryDependencies ++= Seq(Test.junit, Test.scalatest))) dependsOn (s3)

}

object Dependencies {

  object Compile {
    val httpclient = "org.apache.httpcomponents" % "httpclient" % "4.2.1"
    val commonsio = "commons-io" % "commons-io" % "2.4"
    val commonscodec = "commons-codec" % "commons-codec" % "1.7"
  }

  object Test {
    val junit = "junit" % "junit" % "4.10" % "test"
    val scalatest = "org.scalatest" % "scalatest_2.9.2" % "1.8" % "test"
  }
}
