ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

lazy val root = (project in file("."))
  .settings(
    name := "manager",
    idePackagePrefix := Some("org.image_processing.manager")
  )

resolvers += Resolver.file("local-ivy", file(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

libraryDependencies += "com.github.andyglow" %% "typesafe-config-scala" % "2.0.0"
libraryDependencies += "org.scala-lang" %% "toolkit" % "0.2.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.15" % Test
libraryDependencies += "common" %% "common" % "0.1.0-SNAPSHOT"

assembly / assemblyJarName := "manager.jar"