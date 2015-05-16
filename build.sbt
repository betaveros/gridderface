import com.github.retronym.SbtOneJar._

name := "gridderface"

version := "0.3"

scalaVersion := "2.11.5"

libraryDependencies += "org.scala-lang.modules" % "scala-swing_2.11" % "2.0.0-M2"

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")

oneJarSettings
