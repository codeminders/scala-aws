import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object ScalaAWSBuild extends Build {

    val appVersion      = "1.0.0"

    lazy val root = Project(id = "scala-aws",
                            base = file("."),
                            settings = Defaults.defaultSettings ++ assemblySettings 
                            ) aggregate(scalaS3) dependsOn(scalaS3)

    lazy val scalaS3 = Project(id = "scala-s3", 
                           base = file("scala-s3"), settings = Defaults.defaultSettings ++ assemblySettings)
                           
}
