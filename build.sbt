name              := "eSCALAtor"

version           := "0.01.01"

scalaVersion      := "2.11.8"

resolvers         += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.1"
libraryDependencies += "org.scalatest"     %% "scalatest"  % "3.0.0-M15"       % "test"
