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

unmanagedJars in Compile <++= baseDirectory map { base =>
	val libs = base / "lib"
	val dirs = (libs / "squeryl")
	(dirs ** "*.jar").classpath
}
