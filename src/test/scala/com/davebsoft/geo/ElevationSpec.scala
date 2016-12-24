package com.davebsoft.geo

import org.scalatest.{FunSpec, Matchers}

class ElevationSpec extends FunSpec with Matchers {
  describe("Elevation") {
    val elev = new ElevationProvider()

    it("should return a grid of 3D points") {
      val g = elev.grid(-122.1989536, 37.8138747, -122.0087977, 37.9686134)
      println(g)
      succeed
    }
  }
}
