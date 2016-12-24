package com.davebsoft.geo

import java.io.File

import org.geotools.gce.geotiff.GeoTiffReader
import org.geotools.geometry.DirectPosition2D
import org.opengis.coverage.PointOutsideCoverageException

class ElevationProvider {
  case class Point3D(x: Double, y: Double, elevation: Double)
  private val r = new GeoTiffReader(new File("/Users/daveb/devel/elevation/laf-elev.tiff"))
  private val gc = r.read(null)

  def grid(xMin: Double, yMin: Double, xMax: Double, yMax: Double): Seq[Point3D] = {
    val NumPerDimension = 200
    val xSpacing = (xMax - xMin) / NumPerDimension
    val ySpacing = (yMax - yMin) / NumPerDimension

    0 to NumPerDimension flatMap { row =>
      0 to NumPerDimension flatMap { col =>
        at(xMin + col * xSpacing, yMin + row * ySpacing)
      }
    }
  }

  def at(x: Double, y: Double): Option[Point3D] = {
    try {
      Some(
        gc.evaluate(new DirectPosition2D(x, y)) match {
          case e: Array[Float] => Point3D(x, y, e(0))
        })
    } catch {
      case e: PointOutsideCoverageException => None
    }
  }
}
