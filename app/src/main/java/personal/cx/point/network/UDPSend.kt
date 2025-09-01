package personal.cx.point.network

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import personal.cx.point.exception.PointRuntimeException
import personal.cx.point.data.web.ConnectWebData
import personal.cx.point.data.globalSetting
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


class UDPSend {
    private val ips : List<String>? = NetworkIpv4.getLocalNetworkIps()
    
    fun send() {
        if(ips != null){
            //load setting file
            val port = globalSetting.read("library_listen_port")
            val key = globalSetting.read("connect_key")

            //check load result
            if(port == null || key == null){
                throw PointRuntimeException("ERROR: Code error - library_listen_port | connect_key_en | connect_key -> null")
            }
            if(port as Int == 0){
                throw PointRuntimeException("Error: Setting file load failed")
            }

            val udpData = ConnectWebData("UDP", port, key as String, "")

            sendDataToIp(ips, port, Json.encodeToString(udpData))
        }
        else {
            throw PointRuntimeException("Error: Can't get IP address set")
        }
    }
    fun sendDataToIp(ipList: List<String>, port: Int, jsonString: String) {
        ipList.forEach { ip ->
            val socket = DatagramSocket()
            val inetAddress = InetAddress.getByName(ip)
            val packet = DatagramPacket(jsonString.toByteArray(Charsets.UTF_8), jsonString.length, inetAddress, port)

            try {
                socket.send(packet)
            } catch (e: Exception) {
                throw PointRuntimeException("Error: UDP socket send Exception : " + e.message)
            } finally {
                socket.close()
            }
        }
    }
}

