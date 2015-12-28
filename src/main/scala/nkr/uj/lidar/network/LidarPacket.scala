package nkr.uj.lidar.network

import java.lang.Math._

/**
  * Created by sobota on 23.12.15.
  */


case class LidarPacket(index: Index, speed: Speed, data0: ReadData, data1: ReadData, data2: ReadData, data3: ReadData) {


  lazy val data = List[ReadData](data0, data1, data2, data3)

  //Todo read & process speed value
  def toHexString = s"index= ${index.toInt.toHexString}, speed=????, data0= ${data0.toHexString}, data1= ${data1.toHexString}"

  import ReadingNumber._

  def getAxisDistance(readNumber: ReadNumber): Option[(Double, Double)] = {

    readNumber match {

      case READ_0 => {

        val degRad = ((index * 4 + 0) * PI) / 180.0

        data0.getDistance match {
          case Some(x) => Some((cos(degRad) * x, -sin(degRad) * x))
          case None => None
        }
      }

      case READ_1 => {

        val degRad = ((index * 4 + 1) * PI) / 180.0

        data1.getDistance match {
          case Some(x) => Some((cos(degRad) * x, -sin(degRad) * x))
          case None => None
        }
      }

      case READ_2 => {

        val degRad = ((index * 4 + 2) * PI) / 180.0

        data2.getDistance match {
          case Some(x) => Some((cos(degRad) * x, -sin(degRad) * x))
          case None => None
        }
      }

      case READ_3 => {

        val degRad = ((index * 4 + 3) * PI) / 180.0

        data3.getDistance match {
          case Some(x) => Some((cos(degRad) * x, -sin(degRad) * x))
          case None => None
        }
      }
    }

  }

}