import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "SportsMarket"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    jdbc, anorm,
    "mysql" % "mysql-connector-java" % "5.1.22",
    "com.monochromeroad" % "play-xwiki-rendering_2.9.2" % "1.0",
    "org.xwiki.rendering" % "xwiki-rendering-macro-comment" % "4.1.3",
    "jp.t2v" %% "play21.auth" % "0.7"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
    resolvers += "t2v.jp repo" at "http://www.t2v.jp/maven-repo/",
    resolvers += "Monochrmeroad CloudBees Repository" at "http://repository-monochromeroad.forge.cloudbees.com/release"
  )

}
