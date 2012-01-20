name := "nfp-commander"

version := "1.0"

scalaVersion := "2.9.1"

//scala-swing
libraryDependencies <+= scalaVersion { sv =>
  ("org.scala-lang" % "scala-swing" % sv)
}

libraryDependencies += "jfree" % "jfreechart" % "1.0.13"

libraryDependencies += "com.miglayout" % "miglayout" % "3.7.4"

libraryDependencies += "com.toedter" % "jcalendar" % "1.3.2"

libraryDependencies ++= "joda-time" % "joda-time" % "2.0" :: "org.joda" % "joda-convert" % "1.2" :: Nil

libraryDependencies += "com.h2database" % "h2" % "1.3.160"

//squeryl and h2 db
libraryDependencies  ++=  Seq(
  "org.squeryl" %% "squeryl" % "0.9.5-RC1",
  "com.h2database" % "h2" % "1.2.127"
)

//proguard settings
seq(ProguardPlugin.proguardSettings :_*)

proguardOptions ++= Seq(
  keepMain("nfp.view.Main"),
  "-keep class scala.ScalaObject { *; }",
  "-keep class net.sf.cglib.proxy.MethodProxy { *; }",
  "-keep class net.sf.cglib.core.ReflectUtils { *; }",
  "-keep class net.sf.cglib.proxy.MethodInterceptor{ *; }",
  "-keep class net.sf.cglib.reflect.FastClass{ *; }",
  "-keep class org.jfree.chart.resources.JFreeChartResources { *; }",
  "-keep class org.jfree.resources.JCommonResources { *; }"
)

