package personal.cx.point.network

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import personal.cx.point.exception.PointRuntimeException
import personal.cx.point.data.web.ConnectWebData
import personal.cx.point.data.globalSetting
import personal.cx.point.data.innerDatabase
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Socket

class UDPListen {
    private val bufferSize = 1024

    companion object{
        private var listening : Boolean = false
    }

    fun listen(){
        runBlocking {
            try {
                //load port data
                val port = globalSetting.read("UDP_listen_port")

                if(port == null){
                    throw PointRuntimeException("ERROR: Code error - UDP_listen_port -> null")
                }
                if(port as Int == 0){
                    throw PointRuntimeException("Error: Setting file load failed")
                }
                //start listen
                DatagramSocket(port).use { socket ->

                    // 创建缓冲区
                    val buffer = ByteArray(bufferSize)

                    // 创建数据包用于接收数据
                    val packet = DatagramPacket(buffer, buffer.size)

                    while (true) {
                        socket.receive(packet)

                        val address = packet.address

                        val data = String(packet.data, 0, packet.length)
                        launch {
                            udpDataHandle(address.toString(), data)
                        }
                    }
                }
            } catch (e: Exception) {
                throw PointRuntimeException("Error: UDPListen.listen() exception - " + e.message)
            }
        }
    }
    private fun udpDataHandle(address: String, data: String){
        //load setting
        val port = globalSetting.read("library_listen_port")
        val keyEn = globalSetting.read("connect_key_en")
        val key = globalSetting.read("connect_key")

        if(port == null || keyEn == null || key == null){
            throw PointRuntimeException("ERROR: Code error - library_listen_port | connect_key_en | connect_key -> null")
        }
        if(port as Int == 0){
            throw PointRuntimeException("Error: Setting file load failed")
        }

        val connectWebData = Json.decodeFromString<ConnectWebData>(data)

        if(!(keyEn as Boolean) || key as String == connectWebData.key){
            try{
                //connect
                val socket = Socket(address, connectWebData.port)
                val inputStream = DataInputStream(socket.getInputStream())
                val outputStream = DataOutputStream(socket.getOutputStream())
                //build information
                val information = ConnectWebData(WebMessage.TYPE_UDP, port, "", innerDatabase.toInfoData().toString())
                //send
                outputStream.writeUTF(information.toString())
                when(inputStream.readUTF()){
                    WebMessage.MISTAKE_KEY -> null//TODO
                }
            }catch(e: Exception) {
                throw PointRuntimeException("Error: UDPListen.udpDataHandle() exception - " + e.message)
            }
        }
    }
}