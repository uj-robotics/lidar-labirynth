package nkr.uj.lidar.graphics

import javafx.event.EventHandler
import javafx.stage.WindowEvent

import nkr.uj.lidar.network.NetworkConnector
import nkr.uj.lidar.network.ReadingNumber._

import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.Platform._
import scalafx.application.{JFXApp, Platform}
import scalafx.scene.Scene
import scalafx.scene.canvas.{Canvas, GraphicsContext}
import scalafx.scene.paint.Color


/**
  * Created by sobota on 27.12.15.
  */
object GuiMain extends JFXApp {

  val (width, height) = (600.0, 600.0)
  val (centerWidth, centerHeight) = (width / 2, height / 2)

  val canvas = new Canvas(width, height)
  implicit val gc = canvas.graphicsContext2D

  canvas.translateX = 0
  canvas.translateY = 0

  drawRobot()
  /*
      gc.beginPath()
      gc.strokeLine(centerHeight, centerHeight, 30, 30)
      gc.strokeLine(centerHeight, centerHeight, 30, -30)

      gc.closePath()
  */

  /*  drawDistanceVector((-4 * 100 * 10, -4 * 100 * 10))
    drawDistanceVector((3 * 100 * 10, 3 * 100 * 10))

    drawDistanceVector((-6 * 100 * 10, 6 * 100 * 10))
    drawDistanceVector((6 * 100 * 10, -6 * 100 * 10))
  drawDistanceVector((-0 * 100 * 10, 0 * 100 * 10))*/

  stage = new PrimaryStage {
    title = "Distance Vectors"

    onCloseRequest = new EventHandler[WindowEvent] {
      override def handle(event: WindowEvent): Unit = {

        connector.finish()
        Platform.exit()
      }
    }
    scene = new Scene(GuiMain.width, GuiMain.height) {
      content = canvas
    }

  }
  private val connector = NetworkConnector { packet =>

    runLater {
      packet.getAxisDistance(READ_0) match {

        case Some(n) => drawDistanceVector(n)

        case None => (0, 0)

      }

      packet.getAxisDistance(READ_1) match {

        case Some(n) => drawDistanceVector(n)

        case None => (0, 0)
      }

      packet.getAxisDistance(READ_2) match {

        case Some(n) => drawDistanceVector(n)

        case None => (0, 0)
      }

      packet.getAxisDistance(READ_3) match {

        case Some(n) => drawDistanceVector(n)

        case None => (0, 0)
      }
    }
  }

  def drawRobot()(implicit g: GraphicsContext) = {

    val (roboWidth, roboHeigh) = (50.0, 50.0)

    g.beginPath()
    g.fill = Color.Green
    g.fillRect((centerWidth - (roboWidth / 2)), (centerHeight - (roboHeigh / 2)), roboHeigh, roboHeigh)
    g.closePath()
  }

  println("Started")
  for (x <- 1 to 100 * 100 * 100)
    connector()


  def drawDistanceVector(lidarCordinatesData: (Double, Double))(implicit g: GraphicsContext): Unit = {

    /**
      * This function handles LIDAR distance values and normalize it to canvas size
      *
      * @return
      * dist._1 x coordinate belongs to (-Canvas_Width/2, Canvas_Width/2)
      * dist._2 y coordinate belongs to (-Canvas_Height/2, Canvas_Height/2)
      */
    //todo handle case when distance over 6M
    val normalizeDistance = { (dist: (Double, Double)) =>

      val canv = g.getCanvas //todo pass g explicitly

      val MAX_LIDAR_RANGE = 6 * 100 * 10 // max distance in mm
    val CANVAS_MARGIN = 10

      val (canvasWithCenter, canvasHeightCenter) = (canv.getWidth / 2, canv.getHeight / 2)
      val (percentX, percentY) = ((dist._1 / MAX_LIDAR_RANGE) * 100, (dist._2 / MAX_LIDAR_RANGE) * 100)

      val endWidth = ((canvasWithCenter - CANVAS_MARGIN) / 100) * percentX
      val endHeight = ((canvasHeightCenter - CANVAS_MARGIN) / 100) * percentY

      (endWidth, endHeight)
    }

    val normalDist = normalizeDistance(lidarCordinatesData)

    /**
      * This transform normalized coordinates to ScalaFX coordinates
      */
    val transformed = (normalizedDist: (Double, Double)) => normalDist match {

      case (x, y) if x < 0 && y < 0 => (centerWidth + x, centerHeight - y)
      case (x, y) if x > 0 && y > 0 => (centerWidth + x, centerHeight - y)

      case (x, y) if x < 0 && y > 0 => (centerWidth + x, centerHeight - y)
      case (x, y) if x > 0 && y < 0 => (centerWidth + x, centerHeight - y)

      case (0, 0) => (centerWidth, centerHeight)
    }

    val vectorEndPoint = transformed(normalDist)

    g.beginPath()
    g.lineWidth = 3
    g.strokeLine(centerWidth, centerHeight, vectorEndPoint._1, vectorEndPoint._2)
    g.closePath()

  }
}
