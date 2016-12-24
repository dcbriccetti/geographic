name := "geographic"

version := "1.0"

scalaVersion := "2.12.1"

resolvers ++= List(
  Classpaths.typesafeReleases,
  "Geotools"            at "http://download.osgeo.org/webdav/geotools/",
  "GeoSolutions"        at "http://maven.geo-solutions.it"
)

libraryDependencies ++= Seq(
  "org.scala-lang.modules"     %% "scala-xml"                       % "1.0.6",
  "it.geosolutions.imageio-ext" % "imageio-ext-tiff"                % "1.1.17",
  "org.jaitools"                % "jt-vectorize"                    % "1.4.0",
  "javax.media"                 % "jai_core"                        % "1.1.3",
  "com.github.jai-imageio"      % "jai-imageio-core"                % "1.3.0",
  "org.geotools"                % "geotools"                        % "15.1",
  "org.geotools"                % "gt-cql"                          % "15.1",
  "org.geotools"                % "gt-geotiff"                      % "15.1",
  "org.geotools"                % "gt-epsg-hsql"                    % "15.1",
  "org.geotools"                % "gt-referencing"                  % "15.1",
  "org.geotools"                % "gt-shapefile"                    % "15.1",
  "org.geotools.jdbc"           % "gt-jdbc-postgis"                 % "15.1",
  // Install Processing native libraries manually, unless you know how to do it with SBT
  "org.scalatest"              %% "scalatest"                       % "3.0.0"
)
