package com.davebsoft.geo

import java.io._

import processing.core.{PApplet, PConstants}

class ThreeDSpaceViewer extends PApplet {
  val Area = "Lafayette, CA"
  val PlaceTypes = Seq("school", "park", "book_store", "parking", "museum").par // https://developers.google.com/places/supported_types
  val PlaceSearchRadiusMeters = 4000 // 10,000 max, according to API doc
  val ScreenWidth = 1440
  val ScreenHeight = 1440

  val elevationProvider = new ElevationProvider()
  val googleServices = GoogleServices(elevationProvider)

  val places: Seq[Place] = cache {
    for {
      (areaLat, areaLong) <- googleServices.findArea(Area).toSeq
      placeType           <- PlaceTypes
      place               <- googleServices.findNearbyPlaces(areaLat, areaLong, placeType, PlaceSearchRadiusMeters)
    } yield place
  }

  val ScaleFactor = Math.min(ScreenHeight, ScreenWidth)

  var globalXRot = 0F
  var globalZRot = 0F

  abstract class Transformer(property: Place => Double) {
    val min:    Double = if (places.nonEmpty) places.map(property).min else 0
    val max:    Double = if (places.nonEmpty) places.map(property).max else 0
    val range:  Double = max - min
    def normalizeAndScale(value: Double): Float = (normalize(value) * ScaleFactor).toFloat
    def normalize(value: Double): Double = (value - min) / range
    def toScreen(value: Double): Float
  }
  object latTransformer extends Transformer(_.lat) {
    override def toScreen(value: Double) = ScreenHeight - normalizeAndScale(value) - ScreenHeight / 2
  }
  object longTransformer extends Transformer(_.long) {
    override def toScreen(value: Double) = normalizeAndScale(value) - ScreenWidth  / 2
  }
  object elevTransformer extends Transformer(_.elevation) {
    override def toScreen(value: Double) = (normalize(value) * 200).toFloat - ScreenHeight / 4
  }

  val gridElevations = elevationProvider.grid(
    longTransformer.min, latTransformer.min, longTransformer.max, latTransformer.max)

  override def settings(): Unit = {
    size(ScreenWidth, ScreenHeight, PConstants.P3D)
    smooth(8)
  }

  override def setup(): Unit = {
    textFont(createFont("Helvetica", 14))
  }

  override def mouseDragged(): Unit = {
    globalXRot += angleFromMouseUnits(mouseY - pmouseY)
    globalZRot += angleFromMouseUnits(mouseX - pmouseX)
    super.mouseDragged()
  }

  override def draw() = {
    background(0)

    translate(ScreenWidth / 2, ScreenHeight / 2, 0)
    rotateX(globalXRot)
    rotateZ(globalZRot)

    gridElevations foreach { ge =>
      val MinColor = 100
      val c = MinColor + ((255 - MinColor) * elevTransformer.normalize(ge.elevation)).toFloat
      stroke(c, c, 0)
      point(longTransformer.toScreen(ge.x),
        latTransformer.toScreen(ge.y), elevTransformer.toScreen(ge.elevation))
    }

    places foreach {place =>
      pushMatrix()

      translate(longTransformer.toScreen(place.long),
        latTransformer.toScreen(place.lat), elevTransformer.toScreen(place.elevation))
      stroke(0, 255, 0)
      sphere(5)

      rotateZ(-globalZRot)
      rotateX(-globalXRot)
      text(place.name, 7, 3)

      popMatrix()
    }
  }

  private def angleFromMouseUnits(mouseUnits: Int) = mouseUnits.toFloat / ScaleFactor * math.Pi.toFloat

  private def cache[A](compute: => A): A = {
    val cacheFile = new File("/tmp/geo.cache")
    if (cacheFile.exists) {
      val ois = new ObjectInputStream(new FileInputStream(cacheFile))
      val places = ois.readObject.asInstanceOf[A]
      ois.close()
      places
    } else {
      val places = compute
      val oos = new ObjectOutputStream(new FileOutputStream(cacheFile))
      oos.writeObject(places)
      oos.close()
      places
    }
  }
}

object ThreeDSpaceViewer {
  def main(args: Array[String]): Unit = PApplet.main("com.davebsoft.geo.ThreeDSpaceViewer")
}

@SerialVersionUID(1L)
case class Place(name: String, vicinity: String, lat: Double, long: Double, elevation: Double)
