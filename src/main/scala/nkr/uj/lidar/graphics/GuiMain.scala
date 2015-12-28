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
  drawDistanceVector((-80, -90))
  drawDistanceVector((80, 90))

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

  //Todo repair this calculation on cartesian plane
  def drawDistanceVector(vector: (Double, Double))(implicit g: GraphicsContext): Unit = {

    val transformed = vector match {

      case (x, y) if x < 0 && y < 0 => (centerHeight + Math.abs(x), Math.abs(y))
      case c => c
    }

    val margin = 20
    val MAX_LIDAR_RANGE = 6 * 100 * 10 // max distance in mm
    val c = g.getCanvas


    val endWidth = ((c.getWidth / 2 - margin) / MAX_LIDAR_RANGE) * transformed._1
    val endHeight = ((c.getHeight / 2 - margin) / MAX_LIDAR_RANGE) * transformed._2

    g.beginPath()
    g.lineWidth = 3
    g.strokeLine(centerWidth, centerHeight, endWidth, endHeight)
    g.closePath()

  }
}
