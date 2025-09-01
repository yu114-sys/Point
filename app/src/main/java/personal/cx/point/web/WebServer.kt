package personal.cx.point.web

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import personal.cx.point.exception.PointRuntimeException
import personal.cx.point.type.web.ConnectWebData
import personal.cx.point.type.globalSetting
import personal.cx.point.type.listenData
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket

class WebServer {
    private val listen_lock = Object()
    companion object{
        private var listening : Boolean = false
    }
    init {

    }
    fun listen(){
        if(listening == false){
            listening = true
            runBlocking {
                try {
                    //load port data
                    val port = globalSetting.read("library_listen_port")

                    if(port == null){
                        throw PointRuntimeException("ERROR: Code error - library_listen_port -> null")
                    }
                    if(port as Int == 0){
                        throw PointRuntimeException("Error: Setting file load failed")
                    }
                    //binding port
                    val serverSocket = ServerSocket(port)
                    while (true) {
                        val clientSocket = serverSocket.accept()

                        launch {
                            listenDataHandle(clientSocket)
                        }
                    }
                } catch (e: Exception) {
                    throw PointRuntimeException("Error: UDP socket listen Exception : " + e.message)
                } finally {
                    listening = false 
                }
            }
        }
        else{
            throw PointRuntimeException("ERROR: WebServer.listen() has been activated twice")
        }
    }
    private fun listenDataHandle(clientSocket: Socket){
        try{
            val inputData = DataInputStream(clientSocket.getInputStream())
            val outputData = DataOutputStream(clientSocket.getOutputStream())

            val connectWebData = Json.decodeFromString<ConnectWebData>(inputData.readUTF())

            val keyEn = globalSetting.read("connect_key_en")
            val key = globalSetting.read("connect_key")

            if(keyEn == null || key == null){
                throw PointRuntimeException("ERROR: Code error - connect_key_en | connect_key -> null")
            }

            if(!(keyEn as Boolean) || key as String == connectWebData.key){
                when(connectWebData.type){
                    WebMessage.TYPE_UDP -> udpDataHandle(clientSocket.inetAddress.toString(), clientSocket.port, connectWebData.key, connectWebData.information)
                }

                outputData.writeUTF(WebMessage.OK)
            }
            else{
                outputData.writeUTF(WebMessage.MISTAKE_KEY)
            }
        }
        catch (e: Exception){
            throw PointRuntimeException("Error: UDP socket listen Exception : " + e.message)
        }
        finally {
            clientSocket.close()
        }
    }
    private fun udpDataHandle(address: String, port: Int, key:String, jsonData: String){

        val infoData = Json.decodeFromString<List<InfoData>>(jsonData)
        val libraryData = mutableListOf<LibraryData>()

        for(i in infoData){
            libraryData.add(LibraryData(address, port, key, "", i))
        }
        listenData.add(libraryData.toList())
    }
}