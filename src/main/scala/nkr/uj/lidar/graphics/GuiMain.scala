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

  val (windowWidth, windowHeight) = (600.0, 600.0)
  val (centerWindowWidth, centerWindowHeight) = (windowWidth / 2, windowHeight / 2)

  val canvas = new Canvas(windowWidth, windowHeight)
  implicit val gc = canvas.graphicsContext2D

  val dataProcessor = LidarDataProcessor(drawDistanceVector)

  canvas.translateX = 0
  canvas.translateY = 0

  drawRobot()

  stage = new PrimaryStage {
    title = "Distance Vectors"

    onCloseRequest = new EventHandler[WindowEvent] {
      override def handle(event: WindowEvent): Unit = {

        dataProcessor.finish()
        Platform.exit()
      }
    }
    scene = new Scene(GuiMain.windowWidth, GuiMain.windowHeight) {
      content = canvas
    }

  }
  val (canvasWidth, canvasHeight) = (canvas.getWidth, canvas.getHeight)
  val (centerCanvasWidth, centerCanvasHeight) = (canvas.getWidth / 2, canvas.getHeight / 2) //todo repair canvas size caluculation

  def drawRobot()(implicit g: GraphicsContext) = {

    val (roboWidth, roboHeigh) = (50.0, 50.0)

    g.beginPath()
    g.fill = Color.Green
    g.fillRect((centerWindowWidth - (roboWidth / 2)), (centerWindowHeight - (roboHeigh / 2)), roboWidth, roboHeigh)
    g.closePath()
  }

  def drawDistanceVector(vectorEnd: ((Double, Double)) => (Double, Double)): Unit = {

    val vectorEndPoint = vectorEnd(windowWidth, windowHeight)

    runLater {

      gc.beginPath()
      gc.lineWidth = 2

      gc.strokeLine(centerWindowWidth, centerWindowHeight, vectorEndPoint._1, vectorEndPoint._2)
      gc.closePath()
    }
  }
}
