ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

lazy val root = (project in file("."))
  .settings(
    name := "format_worker",
    idePackagePrefix := Some("org.image_processing.format_worker")
  )

resolvers += Resolver.file("local-ivy", file(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

libraryDependencies += "com.github.andyglow" %% "typesafe-config-scala" % "2.0.0"
libraryDependencies += "org.scala-lang" %% "toolkit" % "0.2.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.15" % Test
libraryDependencies += "org.image_processing" %% "common" % "0.1.0-SNAPSHOT"
libraryDependencies += "com.sksamuel.scrimage" % "scrimage-core" % "4.1.1"

assembly / assemblyJarName := "format_worker.jar"