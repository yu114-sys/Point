package personal.cx.point.web

import java.net.InetAddress

object NetworkIpv4 {
    fun getLocalNetworkIps(): List<String>? {

        val localIp = InetAddress.getLocalHost().hostAddress
        val ipParts = localIp?.split(".")
        if(ipParts != null){
            if(ipParts.size >= 3){
                val networkIp = "${ipParts[0]}.${ipParts[1]}.${ipParts[2]}."

                return (1..254).map { "$networkIp$it" }
            }
        }
        return null
    }
}