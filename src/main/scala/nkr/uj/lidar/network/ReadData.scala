package nkr.uj.lidar.network

/**
 * Created by sobota on 23.12.15.
 */
case class ReadData(byte0: Byte, byte1: Byte, byte2: Byte, byte3: Byte) {

  lazy val toList: List[Byte] = List(byte0, byte1, byte2, byte3)


  def toHexString: String = s"byte0=[ ${byte0.toInt.toHexString}] byte1=[ ${byte1.toInt.toHexString}] byte2=[ ${byte2.toInt.toHexString}] byte3=[ ${byte3.toInt.toHexString}]"


  /**
   * Distance in mm
   * @return
   */
  def getDistance: Option[Int] = {

    if ((byte1 & 0x80) > 0) return None

    Some(byte0 | ((byte1 & 0x3f) << 8))
  }


  def isStrengthInferior: Boolean = {
    if ((byte1 & 0x40) > 0) return true

    false
  }
}