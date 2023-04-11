ThisBuild / version := "1.0"

ThisBuild / scalaVersion := "2.12.12"

val sparkVersion = "3.2.2"

lazy val root = (project in file("."))
  .settings(
    name := "analytics",
    //Assembly
    assembly / mainClass := some("org.sfu.amazon_review_analysis.analytics.Main"),
    assembly / assemblyJarName := s"amazon-analytics-${version.value}.jar",

    //Libraries
    libraryDependencies += "org.rogach" %% "scallop" % "4.1.0",
    libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
    libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion % "provided"
  )

