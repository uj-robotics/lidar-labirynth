package nkr.uj.lidar.network

import nkr.uj.lidar.network.ReadingNumber._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by sobota on 07.01.16.
  */
case class LidarDataProcessor(paint: (((Double, Double)) => (Double, Double)) => Unit) {

  private val connector = NetworkConnector { packet =>

    packet.getAxisDistance(READ_0) match {

      case Some(n) => {

        paint(calculateCoordinate.curried(n)(_))
      }
      case None => paint(calculateCoordinate.curried((0, 0))(_))

    }

    packet.getAxisDistance(READ_1) match {

      case Some(n) => {
        paint(calculateCoordinate.curried(n)(_))
      }
      case None => paint(calculateCoordinate.curried((0, 0))(_))
    }

    packet.getAxisDistance(READ_2) match {

      case Some(n) => {
        paint(calculateCoordinate.curried(n)(_))
      }
      case None => paint(calculateCoordinate.curried((0, 0))(_))
    }

    packet.getAxisDistance(READ_3) match {

      case Some(n) => {
        paint(calculateCoordinate.curried(n)(_))
      }
      case None => paint(calculateCoordinate.curried((0, 0))(_))
    }
  }

  //run in separated thread
  Future {
    println("Started")
    while (true) connector()
  }

  private val calculateCoordinate = { (lidarCordinatesData: (Double, Double), canvasSize: (Double, Double)) =>

    val (canvasWidthCenter, canvasHeightCenter) = (canvasSize._1 / 2, canvasSize._2 / 2)

    /**
      * This function handles LIDAR distance values and normalize it to canvas size
      *
      * @return
      * dist._1 x coordinate belongs to (-Canvas_Width/2, Canvas_Width/2)
      * dist._2 y coordinate belongs to (-Canvas_Height/2, Canvas_Height/2)
      */
    //todo handle case when distance over 6M
    val normalizeDistance = { (dist: (Double, Double)) =>

      val MAX_LIDAR_RANGE = 6 * 100 * 10 // max distance in mm
    val CANVAS_MARGIN = 10

      val (percentX, percentY) = ((dist._1 / MAX_LIDAR_RANGE) * 100, (dist._2 / MAX_LIDAR_RANGE) * 100)

      val endWidth = ((canvasWidthCenter - CANVAS_MARGIN) / 100) * percentX
      val endHeight = ((canvasHeightCenter - CANVAS_MARGIN) / 100) * percentY

      (endWidth, endHeight)
    }

    val normalDist = normalizeDistance(lidarCordinatesData)

    /**
      * This transform normalized coordinates to ScalaFX coordinates
      */
    val transformed = (normalizedDist: (Double, Double)) => normalDist match {

      case (x, y) if x < 0 && y < 0 => (canvasWidthCenter + x, canvasHeightCenter - y)
      case (x, y) if x > 0 && y > 0 => (canvasWidthCenter + x, canvasHeightCenter - y)

      case (x, y) if x < 0 && y > 0 => (canvasWidthCenter + x, canvasHeightCenter - y)
      case (x, y) if x > 0 && y < 0 => (canvasWidthCenter + x, canvasHeightCenter - y)

      case (0, 0) => (canvasWidthCenter, canvasHeightCenter)
    }

    transformed(normalDist)
  }

  def finish(): Unit = {

    connector.finish()
  }


}
