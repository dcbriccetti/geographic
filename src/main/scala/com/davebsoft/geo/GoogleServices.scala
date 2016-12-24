package com.davebsoft.geo

import java.net.{URL, URLEncoder}
import scala.xml.{NodeSeq, XML}

case class GoogleServices(elevationProvider: ElevationProvider) {
  val ApiKey = Secrets.apiKey  // Get an API key from https://developers.google.com/places/
  val startTime = System.currentTimeMillis

  def findArea(area: String): Option[(Double, Double)] = {
    log("Looking for " + area)
    val areaEncoded = URLEncoder.encode(area, "utf8")

    val response = XML.load(new URL(
      s"https://maps.googleapis.com/maps/api/geocode/xml?key=$ApiKey&address=$areaEncoded"))

    (response \ "status").text match {
      case "OK" => Some(latLong(response \ "result" \ "geometry" \ "location"))
      case _    => None
    }
  }

  def findNearbyPlaces(lat: Double, long: Double, placeType: String, radiusMeters: Int) = {
    log("Looking for " + placeType)
    val placeTypeEncoded = URLEncoder.encode(placeType, "utf8")
    val urlString = s"https://maps.googleapis.com/maps/api/place/nearbysearch/xml?" +
      s"key=$ApiKey&type=$placeTypeEncoded&location=$lat,$long&radius=$radiusMeters"

    val response = XML.load(new URL(urlString))

    (response \ "status").text match {
      case "OK" =>
        val results = response \ "result"
        for {
          result <- results
          name = (result \ "name").text
          vicinity = (result \ "vicinity").text
          (lat, long) = latLong(result \ "geometry" \ "location")
          point3D <- elevationProvider.at(long, lat)
        } yield Place(name, vicinity, lat, long, point3D.elevation)
      case _ =>
        Seq()
    }
  }

  private def latLong(loc: NodeSeq) = {
    def text(elem: String) = (loc \ elem).text
    (text("lat").toDouble, text("lng").toDouble)
  }

  private def log(msg: String): Unit =
    println(Thread.currentThread.getId + s" ${System.currentTimeMillis - startTime} $msg")
}
