val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).getOrElse("0.6.28")
val scalaNativeVersion =
  Option(System.getenv("SCALANATIVE_VERSION")).getOrElse("0.3.9")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"      % "0.6.1")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "0.6.1")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"                   % scalaJSVersion)
addSbtPlugin("org.scala-native"   % "sbt-scala-native"              % scalaNativeVersion)
addSbtPlugin("com.jsuereth"       % "sbt-pgp"                       % "1.1.2")
addSbtPlugin("com.typesafe.sbt"   % "sbt-git"                       % "1.0.0")
addSbtPlugin("org.xerial.sbt"     % "sbt-sonatype"                  % "2.3")

addSbtPlugin("ch.epfl.lamp" % "sbt-dotty" % "0.2.2")
