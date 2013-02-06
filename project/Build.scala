import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "SportsMarket"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    jdbc, anorm,
    "jp.t2v" % "play20.auth_2.9.1" % "0.4",
    "mysql" % "mysql-connector-java" % "5.1.22",
    "com.monochromeroad" % "play-xwiki-rendering_2.9.2" % "1.0",
    "org.xwiki.rendering" % "xwiki-rendering-macro-comment" % "4.1.3"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
    resolvers += "t2v.jp repo" at "http://www.t2v.jp/maven-repo/",
    resolvers += "Monochrmeroad CloudBees Repository" at "http://repository-monochromeroad.forge.cloudbees.com/release"
  )
  
  lazy val play21auth = uri("https://github.com/t2v/play20-auth.git#play21")

  // Only compile the bootstrap bootstrap.less file and any other *.less file in the stylesheets directory
  def customLessEntryPoints(base: File): PathFinder = (
    (base / "app" / "assets" / "stylesheets" / "bootstrap" * "bootstrap.less") +++
    (base / "app" / "assets" / "stylesheets" * "*.less"))

}
