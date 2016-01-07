package nkr.uj.lidar.graphics

import javafx.event.EventHandler
import javafx.stage.WindowEvent

import nkr.uj.lidar.network.LidarDataProcessor

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

  val dataProcessor = LidarDataProcessor(drawDistanceVector)

  canvas.translateX = 0
  canvas.translateY = 0

  drawRobot()

  /*
      gc.beginPath()
      gc.strokeLine(centerHeight, centerHeight, 30, 30)
      gc.strokeLine(centerHeight, centerHeight, 30, -30)

      gc.closePath()


    drawDistanceVector((-4 * 100 * 10, -4 * 100 * 10))
    drawDistanceVector((3 * 100 * 10, 3 * 100 * 10))

    drawDistanceVector((-6 * 100 * 10, 6 * 100 * 10))
    drawDistanceVector((6 * 100 * 10, -6 * 100 * 10))
  drawDistanceVector((-0 * 100 * 10, 0 * 100 * 10))
  */

  stage = new PrimaryStage {
    title = "Distance Vectors"

    onCloseRequest = new EventHandler[WindowEvent] {
      override def handle(event: WindowEvent): Unit = {

        dataProcessor.finish()
        Platform.exit()
      }
    }
    scene = new Scene(GuiMain.width, GuiMain.height) {
      content = canvas
    }

  }

  def drawRobot()(implicit g: GraphicsContext) = {

    val (roboWidth, roboHeigh) = (50.0, 50.0)

    g.beginPath()
    g.fill = Color.Green
    g.fillRect((centerWidth - (roboWidth / 2)), (centerHeight - (roboHeigh / 2)), roboHeigh, roboHeigh)
    g.closePath()
  }

  def drawDistanceVector(vectorEnd: ((Double, Double)) => (Double, Double)): Unit = {

    val vectorEndPoint = vectorEnd(canvas.getWidth, canvas.getHeight)

    runLater {

      gc.beginPath()
      gc.lineWidth = 2

      gc.strokeLine(centerWidth, centerHeight, vectorEndPoint._1, vectorEndPoint._2)
      gc.closePath()
    }
  }
}
