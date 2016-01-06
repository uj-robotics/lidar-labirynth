package nkr.uj.lidar.network

import nkr.uj.lidar.network.ReadingNumber._

/**
  * Created by sobota on 23.12.15.
  */
object Main extends App {

  private val connector = NetworkConnector { packet =>

    packet.data.foreach(_.getDistance match {

      case Some(x) => println(s"Distance mm= ${x}")
      case _ => None
    })

    println(s"Axis Distance${packet.getAxisDistance(READ_1).getOrElse(-111111)}")

    println(packet.toHexString)

  }

  println("Started")
  for (x <- 1 to 100 * 100 * 10000)
    connector()


  Console.in.readLine()
  connector.finish()
}


