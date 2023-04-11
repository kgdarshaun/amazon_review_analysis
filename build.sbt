ThisBuild / version := "1.0"

ThisBuild / scalaVersion := "2.12.12"

val sparkVersion = "3.2.2"

lazy val root = (project in file("."))
  .settings(
    name := "amazon_review_analysis",
  )

