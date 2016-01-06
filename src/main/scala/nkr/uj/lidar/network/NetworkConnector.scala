package nkr.uj.lidar.network

import java.net.{DatagramPacket, DatagramSocket}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by sobota on 23.12.15.
  */
case class NetworkConnector(success: LidarPacket => Unit, port: Int = 1080) {

  private[this] val socket: DatagramSocket = new DatagramSocket(port)

  // 19 bytes packet * count
  socket.setReceiveBufferSize(PACKET_SIZE * 2048)


  def apply(success: (LidarPacket => Unit) = success): Unit = {

    val future = Future[DatagramPacket] {

      val recvPacket = new DatagramPacket(new Array(PACKET_SIZE), PACKET_SIZE)
      synchronized(socket).receive(recvPacket)

      recvPacket
    }

    future.onSuccess { case x: DatagramPacket =>

      val packet = buildPacket(x.getData)

      success(packet)
    }


    def buildPacket(recvData: Array[Byte]): LidarPacket = {

      val index: Index = recvData(0)
      val speed: Speed = Array[Byte](recvData(1), recvData(2))
      val data0: ReadData = ReadData(recvData(3), recvData(4), recvData(5), recvData(6))
      val data1: ReadData = ReadData(recvData(7), recvData(8), recvData(9), recvData(10))
      val data2: ReadData = ReadData(recvData(11), recvData(12), recvData(13), recvData(14))
      val data3: ReadData = ReadData(recvData(15), recvData(16), recvData(17), recvData(18))

      val lidarPack = LidarPacket(index, speed, data0, data1, data2, data3)

      lidarPack
    }
  }

  def finish(): Unit = {

    socket.close()
  }

}
