import play.twirl.sbt.SbtTwirl
import scalariform.formatter.preferences._

organization := "io.untyped"
name := "scalaitaly-functional-stream-processing"
version := "0.0.1-SNAPSHOT"
scalaVersion := "2.11.8"

resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

val http4sVersion = "0.12.1"
val rhoVersion = "0.9.0"

libraryDependencies ++= Seq(
  "org.http4s"        %% "http4s-blaze-server"   % http4sVersion,
  "org.http4s"        %% "http4s-dsl"            % http4sVersion,
  "org.http4s"        %% "http4s-twirl"          % http4sVersion,
  "org.http4s"        %% "rho-swagger"           % rhoVersion,
  "org.http4s"        %% "http4s-json4s-jackson" % http4sVersion,
  "org.slf4j"          % "slf4j-simple"          % "1.6.4",
  "org.twitter4j"      % "twitter4j-core"        % "4.0.3",
  "com.typesafe"       % "config"                % "1.2.1",
  "edu.stanford.nlp"   % "stanford-corenlp"      % "3.5.2" artifacts (Artifact("stanford-corenlp", "models"), Artifact("stanford-corenlp")),
  "org.scalaz.stream" %% "scalaz-stream"         % "0.8",
  "org.twitter4j"      % "twitter4j-stream"      % "4.0.3"

)

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.6.4"


initialCommands in console := "import twitter4j._, io.untyped.fstream._,scalaz.stream._,Process._,scalaz.concurrent._"

scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignParameters, false)
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 90)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(PreserveDanglingCloseParenthesis, true)
  .setPreference(RewriteArrowSymbols, true)


scalacOptions := Seq("-encoding", "utf8",
                     "-target:jvm-1.8",
                     "-feature",
                     "-language:implicitConversions",
                     "-language:postfixOps",
                     "-unchecked",
                     "-Xfatal-warnings",
                     //"-Xlint"
                     "-deprecation",
                     "-Xlog-reflective-calls"
                     // "-Ywarn-unused",
                     // "-Ywarn-unused-import",
                     // "-Ywarn-dead-code"
                    )


lazy val root = (project in file(".")).enablePlugins(SbtTwirl)

Revolver.settings
